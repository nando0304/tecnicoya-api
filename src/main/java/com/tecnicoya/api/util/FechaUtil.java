package com.tecnicoya.api.util;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public final class FechaUtil {
    private FechaUtil() {
    }

    public static LocalDateTime ahora() {
        return LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
    }
}
