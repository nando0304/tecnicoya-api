package com.tecnicoya.api.dto.response;

import com.tecnicoya.api.entity.ExperienciaLaboral;

import java.time.LocalDate;

/** Experiencia laboral devuelta por la API, con el nombre del técnico. */
public record ExperienciaLaboralResponse(
        Long idExperiencia,
        Long tecnicoId,
        String tecnicoNombre,
        String empresa,
        String cargo,
        String descripcion,
        LocalDate fechaInicio,
        LocalDate fechaFin,
        Boolean actualidad
) {
    public static ExperienciaLaboralResponse desde(ExperienciaLaboral experiencia) {
        return new ExperienciaLaboralResponse(
                experiencia.getIdExperiencia(),
                experiencia.getTecnico().getIdTecnico(),
                experiencia.getTecnico().getUsuario().nombreCompleto(),
                experiencia.getEmpresa(),
                experiencia.getCargo(),
                experiencia.getDescripcion(),
                experiencia.getFechaInicio(),
                experiencia.getFechaFin(),
                experiencia.getActualidad());
    }
}
