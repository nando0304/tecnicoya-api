package com.tecnicoya.api.exception;

import java.time.Duration;

public class DemasiadosIntentosException extends RuntimeException {
    private final Duration esperar;

    public DemasiadosIntentosException(String mensaje, Duration esperar) {
        super(mensaje);
        this.esperar = esperar;
    }

    public Duration getEsperar() {
        return esperar;
    }
}
