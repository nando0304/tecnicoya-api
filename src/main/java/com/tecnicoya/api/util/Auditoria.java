package com.tecnicoya.api.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Logger de auditoría de seguridad.
 *
 * <p>Usa la categoría {@code AUDITORIA}, que {@code logback-spring.xml} envía a un archivo propio
 * ({@code logs/auditoria.log}) separado del log general. Registra inicios de sesión, bloqueos, accesos
 * denegados y cada operación que modifica datos.
 *
 * <p>Nunca se registran contraseñas ni tokens.
 */
public final class Auditoria {

    /** Logger SLF4J de la categoría {@code AUDITORIA} (implementado por Logback). */
    public static final Logger LOG = LoggerFactory.getLogger("AUDITORIA");

    private Auditoria() {
    }
}
