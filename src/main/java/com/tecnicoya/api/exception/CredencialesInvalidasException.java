package com.tecnicoya.api.exception;

/** Correo o contraseña incorrectos, o cuenta que no puede iniciar sesión. Se traduce a HTTP 401. */
public class CredencialesInvalidasException extends RuntimeException {

    public CredencialesInvalidasException(String mensaje) {
        super(mensaje);
    }
}
