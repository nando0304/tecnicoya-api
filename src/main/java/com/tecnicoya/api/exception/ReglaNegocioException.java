package com.tecnicoya.api.exception;

/** La solicitud es sintácticamente válida pero incumple una regla del negocio. Se traduce a HTTP 400. */
public class ReglaNegocioException extends RuntimeException {

    public ReglaNegocioException(String mensaje) {
        super(mensaje);
    }
}
