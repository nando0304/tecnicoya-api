package com.tecnicoya.api.dto.request;

import com.tecnicoya.api.entity.enums.DiaSemana;
import com.tecnicoya.api.entity.enums.EstadoRegistro;
import jakarta.validation.constraints.NotNull;

import java.time.LocalTime;

public record DisponibilidadRequest(

        @NotNull(message = "El id del técnico es obligatorio")
        Long tecnicoId,

        @NotNull(message = "El día de la semana es obligatorio")
        DiaSemana diaSemana,

        @NotNull(message = "La hora de inicio es obligatoria")
        LocalTime horaInicio,

        @NotNull(message = "La hora de fin es obligatoria")
        LocalTime horaFin,

        EstadoRegistro estado
) {
}
