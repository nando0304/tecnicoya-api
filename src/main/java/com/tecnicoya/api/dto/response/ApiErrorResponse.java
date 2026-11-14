package com.tecnicoya.api.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/** Cuerpo JSON estándar para todas las respuestas de error de la API. */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiErrorResponse(
        LocalDateTime timestamp,
        int status,
        String error,
        String mensaje,
        String ruta,
        Map<String, String> errores
) {

    /** Copia defensiva e inmutable de los errores por campo (conserva el orden en que se detectaron). */
    public ApiErrorResponse {
        errores = errores == null ? null : Collections.unmodifiableMap(new LinkedHashMap<>(errores));
    }
}
