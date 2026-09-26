package com.tecnicoya.api.dto.request;

import com.tecnicoya.api.entity.enums.EstadoVerificacion;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record TecnicoRequest(

        @NotNull(message = "El id del usuario es obligatorio")
        Long usuarioId,

        @NotBlank(message = "La especialidad es obligatoria")
        @Size(max = 100, message = "La especialidad no debe superar los 100 caracteres")
        String especialidad,

        @Size(max = 1000, message = "La descripción no debe superar los 1000 caracteres")
        String descripcion,

        EstadoVerificacion estadoVerificacion
) {
}
