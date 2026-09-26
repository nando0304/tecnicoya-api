package com.tecnicoya.api.dto.request;

import com.tecnicoya.api.entity.enums.EstadoServicio;
import com.tecnicoya.api.entity.enums.Prioridad;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

/**
 * Datos de un servicio. La fecha de solicitud la asigna el sistema al crearlo;
 * la fecha de cierre solo se acepta en estados FINALIZADO o CANCELADO.
 */
public record ServicioRequest(

        @NotNull(message = "El id del cliente es obligatorio")
        Long clienteId,

        Long tecnicoId,

        @NotBlank(message = "El título es obligatorio")
        @Size(max = 150, message = "El título no debe superar los 150 caracteres")
        String titulo,

        @NotBlank(message = "La descripción del problema es obligatoria")
        @Size(max = 1000, message = "La descripción del problema no debe superar los 1000 caracteres")
        String descripcionProblema,

        EstadoServicio estadoServicio,

        Prioridad prioridad,

        LocalDateTime fechaServicio,

        LocalDateTime fechaCierre
) {
}
