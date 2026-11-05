package com.tecnicoya.api.exception;

import java.time.Duration;

/**
 * Se superó el número de intentos fallidos de inicio de sesión permitidos (protección contra ataques de fuerza
 * bruta). Se traduce a HTTP 429 con la cabecera {@code Retry-After}.
 */
public class DemasiadosIntentosException extends RuntimeException {

    private final transient Duration esperar;

    /**
     * @param mensaje mensaje para el usuario
     * @param esperar tiempo que debe esperar antes de volver a intentarlo
     */
    public DemasiadosIntentosException(String mensaje, Duration esperar) {
        super(mensaje);
        this.esperar = esperar;
    }

    /** Tiempo que el cliente debe esperar antes de volver a intentarlo. */
    public Duration getEsperar() {
        return esperar;
    }
}
