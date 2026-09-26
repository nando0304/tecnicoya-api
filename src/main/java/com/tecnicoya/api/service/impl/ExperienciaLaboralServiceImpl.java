package com.tecnicoya.api.service.impl;

import com.tecnicoya.api.dto.request.ExperienciaLaboralRequest;
import com.tecnicoya.api.dto.response.ExperienciaLaboralResponse;
import com.tecnicoya.api.entity.ExperienciaLaboral;
import com.tecnicoya.api.entity.Tecnico;
import com.tecnicoya.api.exception.RecursoNoEncontradoException;
import com.tecnicoya.api.exception.ReglaNegocioException;
import com.tecnicoya.api.repository.ExperienciaLaboralRepository;
import com.tecnicoya.api.repository.TecnicoRepository;
import com.tecnicoya.api.service.ExperienciaLaboralService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ExperienciaLaboralServiceImpl implements ExperienciaLaboralService {

    private final ExperienciaLaboralRepository experienciaLaboralRepository;
    private final TecnicoRepository tecnicoRepository;

    @Override
    public List<ExperienciaLaboralResponse> listar() {
        return experienciaLaboralRepository.findAll().stream().map(ExperienciaLaboralResponse::desde).toList();
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
        return ExperienciaLaboralResponse.desde(experienciaLaboralRepository.save(experiencia));
    }

    @Override
    @Transactional
    public ExperienciaLaboralResponse actualizar(Long id, ExperienciaLaboralRequest request) {
        ExperienciaLaboral experiencia = buscar(id);
        aplicarDatos(request, experiencia);
        return ExperienciaLaboralResponse.desde(experienciaLaboralRepository.save(experiencia));
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        experienciaLaboralRepository.delete(buscar(id));
        experienciaLaboralRepository.flush();
    }

    private ExperienciaLaboral buscar(Long id) {
        return experienciaLaboralRepository.findById(id)
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
        Tecnico tecnico = tecnicoRepository.findById(request.tecnicoId())
                .orElseThrow(() -> new RecursoNoEncontradoException("el técnico", request.tecnicoId()));

        experiencia.setTecnico(tecnico);
        experiencia.setEmpresa(request.empresa().trim());
        experiencia.setCargo(request.cargo().trim());
        experiencia.setDescripcion(request.descripcion());
        experiencia.setFechaInicio(request.fechaInicio());
        experiencia.setFechaFin(request.fechaFin());
        experiencia.setActualidad(request.actualidad());
    }
}
