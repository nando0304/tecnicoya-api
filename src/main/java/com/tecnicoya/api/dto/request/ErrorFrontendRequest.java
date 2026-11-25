package com.tecnicoya.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ErrorFrontendRequest(
        @NotBlank(message = "El mensaje del error es obligatorio")
        @Size(max = 1000, message = "El mensaje no puede superar los 1000 caracteres")
        String mensaje,

        @Size(max = 8000, message = "La pila no puede superar los 8000 caracteres")
        String pila,

        @Size(max = 1000, message = "La URL no puede superar los 1000 caracteres")
        String url,

        @Size(max = 500, message = "El navegador no puede superar los 500 caracteres")
        String navegador,

        @Size(max = 30, message = "El tipo no puede superar los 30 caracteres")
        String tipo
) {
}
