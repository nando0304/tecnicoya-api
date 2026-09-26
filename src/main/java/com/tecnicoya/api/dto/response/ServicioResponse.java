package com.tecnicoya.api.dto.response;

import com.tecnicoya.api.entity.Servicio;
import com.tecnicoya.api.entity.Tecnico;
import com.tecnicoya.api.entity.enums.EstadoServicio;
import com.tecnicoya.api.entity.enums.Prioridad;

import java.time.LocalDateTime;

public record ServicioResponse(
        Long idServicio,
        Long clienteId,
        String clienteNombre,
        Long tecnicoId,
        String tecnicoNombre,
        String titulo,
        String descripcionProblema,
        EstadoServicio estadoServicio,
        Prioridad prioridad,
        LocalDateTime fechaSolicitud,
        LocalDateTime fechaServicio,
        LocalDateTime fechaCierre
) {
    public static ServicioResponse desde(Servicio servicio) {
        Tecnico tecnico = servicio.getTecnico();
        return new ServicioResponse(
                servicio.getIdServicio(),
                servicio.getCliente().getIdUsuario(),
                servicio.getCliente().nombreCompleto(),
                tecnico != null ? tecnico.getIdTecnico() : null,
                tecnico != null ? tecnico.getUsuario().nombreCompleto() : null,
                servicio.getTitulo(),
                servicio.getDescripcionProblema(),
                servicio.getEstadoServicio(),
                servicio.getPrioridad(),
                servicio.getFechaSolicitud(),
                servicio.getFechaServicio(),
                servicio.getFechaCierre());
    }
}
