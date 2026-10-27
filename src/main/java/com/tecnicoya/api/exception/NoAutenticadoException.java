package com.tecnicoya.api.exception;

/** La petición no trae un token JWT válido: falta, fue modificado o ya venció. Se traduce a HTTP 401. */
public class NoAutenticadoException extends RuntimeException {

    public NoAutenticadoException(String mensaje) {
        super(mensaje);
    }
}
