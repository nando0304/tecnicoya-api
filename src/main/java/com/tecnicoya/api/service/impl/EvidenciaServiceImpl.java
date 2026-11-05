package com.tecnicoya.api.service.impl;

import com.tecnicoya.api.dao.IEvidenciaDao;
import com.tecnicoya.api.dao.ITecnicoDao;
import com.tecnicoya.api.dto.request.EvidenciaRequest;
import com.tecnicoya.api.dto.response.EvidenciaResponse;
import com.tecnicoya.api.exception.RecursoNoEncontradoException;
import com.tecnicoya.api.model.enums.EstadoValidacion;
import com.tecnicoya.api.model.Evidencia;
import com.tecnicoya.api.model.Tecnico;
import com.tecnicoya.api.service.EvidenciaService;
import com.tecnicoya.api.util.FechaUtil;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** Implementación de {@link EvidenciaService}. */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EvidenciaServiceImpl implements EvidenciaService {

    private final IEvidenciaDao evidenciaDao;
    private final ITecnicoDao tecnicoDao;

    @Override
    public List<EvidenciaResponse> listar() {
        return evidenciaDao.listar().stream().map(EvidenciaResponse::desde).toList();
    }

    @Override
    public EvidenciaResponse obtenerPorId(Long id) {
        return EvidenciaResponse.desde(buscar(id));
    }

    @Override
    @Transactional
    public EvidenciaResponse crear(EvidenciaRequest request) {
        Evidencia evidencia = new Evidencia();
        evidencia.setFechaCarga(FechaUtil.ahora());
        evidencia.setEstadoValidacion(request.estadoValidacion() != null
                ? request.estadoValidacion() : EstadoValidacion.PENDIENTE);
        aplicarDatos(request, evidencia);
        return EvidenciaResponse.desde(evidenciaDao.guardar(evidencia));
    }

    @Override
    @Transactional
    public EvidenciaResponse actualizar(Long id, EvidenciaRequest request) {
        Evidencia evidencia = buscar(id);
        if (request.estadoValidacion() != null) {
            evidencia.setEstadoValidacion(request.estadoValidacion());
        }
        aplicarDatos(request, evidencia);
        return EvidenciaResponse.desde(evidenciaDao.guardar(evidencia));
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        evidenciaDao.eliminar(buscar(id));
    }

    private Evidencia buscar(Long id) {
        return evidenciaDao.buscarPorId(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("la evidencia", id));
    }

    private void aplicarDatos(EvidenciaRequest request, Evidencia evidencia) {
        Tecnico tecnico = tecnicoDao.buscarPorId(request.tecnicoId())
                .orElseThrow(() -> new RecursoNoEncontradoException("el técnico", request.tecnicoId()));
        evidencia.setTecnico(tecnico);
        evidencia.setTipoEvidencia(request.tipoEvidencia());
        evidencia.setUrlArchivo(request.urlArchivo().trim());
        evidencia.setDescripcion(StringUtils.stripToNull(request.descripcion()));
    }
}
