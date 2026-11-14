package com.tecnicoya.api.service.impl;

import com.tecnicoya.api.dao.IExperienciaLaboralDao;
import com.tecnicoya.api.dao.ITecnicoDao;
import com.tecnicoya.api.dto.request.ExperienciaLaboralRequest;
import com.tecnicoya.api.dto.response.ExperienciaLaboralResponse;
import com.tecnicoya.api.exception.RecursoNoEncontradoException;
import com.tecnicoya.api.exception.ReglaNegocioException;
import com.tecnicoya.api.model.ExperienciaLaboral;
import com.tecnicoya.api.model.Tecnico;
import com.tecnicoya.api.security.ControlAcceso;
import com.tecnicoya.api.service.ExperienciaLaboralService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Implementación de {@link ExperienciaLaboralService}.
 *
 * <p>Control de acceso ({@link ControlAcceso}): un técnico solo crea, edita y elimina la experiencia laboral
 * de su propio perfil.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ExperienciaLaboralServiceImpl implements ExperienciaLaboralService {

    private final IExperienciaLaboralDao experienciaLaboralDao;
    private final ITecnicoDao tecnicoDao;
    private final ControlAcceso controlAcceso;

    @Override
    public List<ExperienciaLaboralResponse> listar() {
        return experienciaLaboralDao.listar().stream().map(ExperienciaLaboralResponse::desde).toList();
    }

    @Override
    public ExperienciaLaboralResponse obtenerPorId(Long id) {
        return ExperienciaLaboralResponse.desde(buscar(id));
    }

    @Override
    @Transactional
    public ExperienciaLaboralResponse crear(ExperienciaLaboralRequest request) {
        ExperienciaLaboral experiencia = new ExperienciaLaboral();
        aplicarDatos(request, experiencia);
        return ExperienciaLaboralResponse.desde(experienciaLaboralDao.guardar(experiencia));
    }

    @Override
    @Transactional
    public ExperienciaLaboralResponse actualizar(Long id, ExperienciaLaboralRequest request) {
        ExperienciaLaboral experiencia = buscar(id);
        verificarDueno(experiencia);
        aplicarDatos(request, experiencia);
        return ExperienciaLaboralResponse.desde(experienciaLaboralDao.guardar(experiencia));
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        ExperienciaLaboral experiencia = buscar(id);
        verificarDueno(experiencia);
        experienciaLaboralDao.eliminar(experiencia);
    }

    private void verificarDueno(ExperienciaLaboral experiencia) {
        controlAcceso.exigirPropietario("gestionar la experiencia laboral de otro técnico",
                experiencia.getTecnico().getUsuario().getIdUsuario());
    }

    private ExperienciaLaboral buscar(Long id) {
        return experienciaLaboralDao.buscarPorId(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("la experiencia laboral", id));
    }

    private void aplicarDatos(ExperienciaLaboralRequest request, ExperienciaLaboral experiencia) {
        if (request.actualidad() && request.fechaFin() != null) {
            throw new ReglaNegocioException("Si es el trabajo actual (actualidad = true) no debe indicar fecha de fin");
        }
        if (!request.actualidad() && request.fechaFin() == null) {
            throw new ReglaNegocioException("La fecha de fin es obligatoria cuando no es el trabajo actual");
        }
        if (request.fechaFin() != null && request.fechaFin().isBefore(request.fechaInicio())) {
            throw new ReglaNegocioException("La fecha de fin no puede ser anterior a la fecha de inicio");
        }
        Tecnico tecnico = tecnicoDao.buscarPorId(request.tecnicoId())
                .orElseThrow(() -> new RecursoNoEncontradoException("el técnico", request.tecnicoId()));
        controlAcceso.exigirPropietario("gestionar la experiencia laboral de otro técnico",
                tecnico.getUsuario().getIdUsuario());

        experiencia.setTecnico(tecnico);
        experiencia.setEmpresa(StringUtils.normalizeSpace(request.empresa()));
        experiencia.setCargo(StringUtils.normalizeSpace(request.cargo()));
        experiencia.setDescripcion(StringUtils.stripToNull(request.descripcion()));
        experiencia.setFechaInicio(request.fechaInicio());
        experiencia.setFechaFin(request.fechaFin());
        experiencia.setActualidad(request.actualidad());
    }
}
