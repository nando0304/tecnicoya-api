package com.tecnicoya.api.dto.request;

import com.tecnicoya.api.model.enums.EstadoSuscripcion;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record SuscripcionRequest(
        @NotNull(message = "El id del técnico es obligatorio")
        Long tecnicoId,

        @NotNull(message = "El id del plan es obligatorio")
        Long planId,

        @NotNull(message = "La fecha de inicio es obligatoria")
        LocalDate fechaInicio,

        @NotNull(message = "La fecha de fin es obligatoria")
        LocalDate fechaFin,

        LocalDate fechaCancelacion,

        EstadoSuscripcion estadoSuscripcion
) {
}
