package com.tecnicoya.api.dto.response;

import com.tecnicoya.api.entity.Disponibilidad;
import com.tecnicoya.api.entity.enums.DiaSemana;
import com.tecnicoya.api.entity.enums.EstadoRegistro;

import java.time.LocalTime;

/** Franja de disponibilidad devuelta por la API, con el nombre del técnico. */
public record DisponibilidadResponse(
        Long idDisponibilidad,
        Long tecnicoId,
        String tecnicoNombre,
        DiaSemana diaSemana,
        LocalTime horaInicio,
        LocalTime horaFin,
        EstadoRegistro estado
) {
    public static DisponibilidadResponse desde(Disponibilidad disponibilidad) {
        return new DisponibilidadResponse(
                disponibilidad.getIdDisponibilidad(),
                disponibilidad.getTecnico().getIdTecnico(),
                disponibilidad.getTecnico().getUsuario().nombreCompleto(),
                disponibilidad.getDiaSemana(),
                disponibilidad.getHoraInicio(),
                disponibilidad.getHoraFin(),
                disponibilidad.getEstado());
    }
}
