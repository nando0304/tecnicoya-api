package com.tecnicoya.api.dto.request;

import com.tecnicoya.api.entity.enums.EstadoValidacion;
import com.tecnicoya.api.entity.enums.TipoEvidencia;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record EvidenciaRequest(

        @NotNull(message = "El id del técnico es obligatorio")
        Long tecnicoId,

        @NotNull(message = "El tipo de evidencia es obligatorio")
        TipoEvidencia tipoEvidencia,

        @NotBlank(message = "La URL del archivo es obligatoria")
        @Size(max = 500, message = "La URL del archivo no debe superar los 500 caracteres")
        @Pattern(regexp = "^https?://\\S+$", message = "La URL del archivo debe iniciar con http:// o https://")
        String urlArchivo,

        @Size(max = 500, message = "La descripción no debe superar los 500 caracteres")
        String descripcion,

        EstadoValidacion estadoValidacion
) {
}
