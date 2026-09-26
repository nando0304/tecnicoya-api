package com.tecnicoya.api.service.impl;

import com.tecnicoya.api.dto.request.EvidenciaRequest;
import com.tecnicoya.api.dto.response.EvidenciaResponse;
import com.tecnicoya.api.entity.Evidencia;
import com.tecnicoya.api.entity.Tecnico;
import com.tecnicoya.api.entity.enums.EstadoValidacion;
import com.tecnicoya.api.exception.RecursoNoEncontradoException;
import com.tecnicoya.api.repository.EvidenciaRepository;
import com.tecnicoya.api.repository.TecnicoRepository;
import com.tecnicoya.api.service.EvidenciaService;
import com.tecnicoya.api.util.FechaUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EvidenciaServiceImpl implements EvidenciaService {

    private final EvidenciaRepository evidenciaRepository;
    private final TecnicoRepository tecnicoRepository;

    @Override
    public List<EvidenciaResponse> listar() {
        return evidenciaRepository.findAll().stream().map(EvidenciaResponse::desde).toList();
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
        return EvidenciaResponse.desde(evidenciaRepository.save(evidencia));
    }

    @Override
    @Transactional
    public EvidenciaResponse actualizar(Long id, EvidenciaRequest request) {
        Evidencia evidencia = buscar(id);
        if (request.estadoValidacion() != null) {
            evidencia.setEstadoValidacion(request.estadoValidacion());
        }
        aplicarDatos(request, evidencia);
        return EvidenciaResponse.desde(evidenciaRepository.save(evidencia));
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        evidenciaRepository.delete(buscar(id));
        evidenciaRepository.flush();
    }

    private Evidencia buscar(Long id) {
        return evidenciaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("la evidencia", id));
    }

    private void aplicarDatos(EvidenciaRequest request, Evidencia evidencia) {
        Tecnico tecnico = tecnicoRepository.findById(request.tecnicoId())
                .orElseThrow(() -> new RecursoNoEncontradoException("el técnico", request.tecnicoId()));
        evidencia.setTecnico(tecnico);
        evidencia.setTipoEvidencia(request.tipoEvidencia());
        evidencia.setUrlArchivo(request.urlArchivo().trim());
        evidencia.setDescripcion(request.descripcion());
    }
}
