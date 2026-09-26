package com.tecnicoya.api.util;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public final class FechaUtil {

    private FechaUtil() {
    }

    /** Fecha y hora actual sin fracciones de segundo, igual a como la guarda una columna DATETIME. */
    public static LocalDateTime ahora() {
        return LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
    }
}
