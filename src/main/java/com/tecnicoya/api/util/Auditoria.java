package com.tecnicoya.api.util;

import org.apache.commons.lang3.StringUtils;
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
 *
 * <p>Los datos que vienen del cliente (correo, ruta, IP) se pasan por {@link #limpiar(Object)} antes de
 * registrarlos, para evitar la inyección de líneas falsas en el log (CRLF injection, CWE-117). El patrón de
 * {@code logback-spring.xml} también reemplaza los saltos de línea: son dos capas de defensa.
 */
public final class Auditoria {

    /** Logger SLF4J de la categoría {@code AUDITORIA} (implementado por Logback). */
    public static final Logger LOG = LoggerFactory.getLogger("AUDITORIA");

    /**
     * Neutraliza los saltos de línea de un dato antes de registrarlo: un atacante no puede cerrar la línea
     * actual y escribir una entrada falsa (por ejemplo, un LOGIN_EXITOSO inventado).
     *
     * @param valor dato que viene del cliente (puede ser {@code null})
     * @return el texto con los retornos de carro y saltos de línea escapados (se ven como \r y \n),
     *         recortado a 200 caracteres
     */
    public static String limpiar(Object valor) {
        if (valor == null) {
            return null;
        }
        // El reemplazo va al final: así FindSecBugs (SAST) reconoce que el valor quedó neutralizado
        return StringUtils.abbreviate(String.valueOf(valor), 200).replace("\r", "\\r").replace("\n", "\\n");
    }

    private Auditoria() {
    }
}
