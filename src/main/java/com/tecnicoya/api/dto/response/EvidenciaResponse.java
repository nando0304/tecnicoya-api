package com.tecnicoya.api.dto.response;

import com.tecnicoya.api.entity.Evidencia;
import com.tecnicoya.api.entity.enums.EstadoValidacion;
import com.tecnicoya.api.entity.enums.TipoEvidencia;

import java.time.LocalDateTime;

/** Evidencia devuelta por la API, con el nombre del técnico. */
public record EvidenciaResponse(
        Long idEvidencia,
        Long tecnicoId,
        String tecnicoNombre,
        TipoEvidencia tipoEvidencia,
        String urlArchivo,
        String descripcion,
        EstadoValidacion estadoValidacion,
        LocalDateTime fechaCarga
) {
    public static EvidenciaResponse desde(Evidencia evidencia) {
        return new EvidenciaResponse(
                evidencia.getIdEvidencia(),
                evidencia.getTecnico().getIdTecnico(),
                evidencia.getTecnico().getUsuario().nombreCompleto(),
                evidencia.getTipoEvidencia(),
                evidencia.getUrlArchivo(),
                evidencia.getDescripcion(),
                evidencia.getEstadoValidacion(),
                evidencia.getFechaCarga());
    }
}
