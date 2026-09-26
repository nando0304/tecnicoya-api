package com.tecnicoya.api.dto.response;

import com.tecnicoya.api.entity.Calificacion;
import com.tecnicoya.api.entity.Servicio;
import com.tecnicoya.api.entity.Tecnico;

import java.time.LocalDateTime;

public record CalificacionResponse(
        Long idCalificacion,
        Long servicioId,
        String servicioTitulo,
        Long tecnicoId,
        String tecnicoNombre,
        Integer puntuacion,
        String comentario,
        LocalDateTime fechaCalificacion
) {
    public static CalificacionResponse desde(Calificacion calificacion) {
        Servicio servicio = calificacion.getServicio();
        Tecnico tecnico = servicio.getTecnico();
        return new CalificacionResponse(
                calificacion.getIdCalificacion(),
                servicio.getIdServicio(),
                servicio.getTitulo(),
                tecnico != null ? tecnico.getIdTecnico() : null,
                tecnico != null ? tecnico.getUsuario().nombreCompleto() : null,
                calificacion.getPuntuacion(),
                calificacion.getComentario(),
                calificacion.getFechaCalificacion());
    }
}
