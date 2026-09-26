package com.tecnicoya.api.dto.response;

import com.tecnicoya.api.entity.Suscripcion;
import com.tecnicoya.api.entity.enums.EstadoSuscripcion;

import java.time.LocalDate;

public record SuscripcionResponse(
        Long idSuscripcion,
        Long tecnicoId,
        String tecnicoNombre,
        Long planId,
        String nombrePlan,
        LocalDate fechaInicio,
        LocalDate fechaFin,
        LocalDate fechaCancelacion,
        EstadoSuscripcion estadoSuscripcion
) {
    public static SuscripcionResponse desde(Suscripcion suscripcion) {
        return new SuscripcionResponse(
                suscripcion.getIdSuscripcion(),
                suscripcion.getTecnico().getIdTecnico(),
                suscripcion.getTecnico().getUsuario().nombreCompleto(),
                suscripcion.getPlan().getIdPlan(),
                suscripcion.getPlan().getNombrePlan(),
                suscripcion.getFechaInicio(),
                suscripcion.getFechaFin(),
                suscripcion.getFechaCancelacion(),
                suscripcion.getEstadoSuscripcion());
    }
}
