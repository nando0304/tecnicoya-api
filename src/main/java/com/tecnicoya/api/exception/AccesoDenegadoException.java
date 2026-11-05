package com.tecnicoya.api.exception;

/**
 * El usuario inició sesión, pero su rol no tiene permiso para usar el recurso (principio de mínimo privilegio).
 * Se traduce a HTTP 403.
 */
public class AccesoDenegadoException extends RuntimeException {

    public AccesoDenegadoException(String mensaje) {
        super(mensaje);
    }
}
