package com.tecnicoya.api.exception;

/** Se traduce a HTTP 404. */
public class RecursoNoEncontradoException extends RuntimeException {

    /**
     * @param recurso nombre del recurso con su artículo, p. ej. "el usuario" o "la calificación"
     */
    public RecursoNoEncontradoException(String recurso, Long id) {
        super("No se encontró " + recurso + " con id " + id);
    }

    /**
     * @param mensaje mensaje completo, para recursos que no se identifican con un id numérico (p. ej. archivos)
     */
    public RecursoNoEncontradoException(String mensaje) {
        super(mensaje);
    }
}
