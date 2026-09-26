package com.tecnicoya.api.exception;

/** Datos duplicados o en conflicto con el estado actual (p. ej. correo ya registrado). Se traduce a HTTP 409. */
public class ConflictoException extends RuntimeException {

    public ConflictoException(String mensaje) {
        super(mensaje);
    }
}
