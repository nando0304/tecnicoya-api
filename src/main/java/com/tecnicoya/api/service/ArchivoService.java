package com.tecnicoya.api.service;

import com.tecnicoya.api.exception.RecursoNoEncontradoException;
import com.tecnicoya.api.exception.ReglaNegocioException;
import org.springframework.core.io.InputStreamSource;
import org.springframework.core.io.Resource;

/**
 * Almacenamiento de los archivos que respaldan las evidencias de los técnicos y los comprobantes de pago.
 *
 * <p>Reglas:
 * <ul>
 *   <li>Solo se aceptan PDF e imágenes JPG, PNG o WEBP, según la extensión del nombre original.</li>
 *   <li>Del nombre original se descarta cualquier ruta (por ejemplo {@code C:\fakepath\} o {@code ../}).</li>
 *   <li>El archivo se guarda con su huella SHA-256 como nombre: el mismo contenido se guarda una sola vez
 *       y el nombre no se puede adivinar sin tener el archivo.</li>
 *   <li>El tamaño máximo lo fija {@code spring.servlet.multipart.max-file-size}.</li>
 * </ul>
 */
public interface ArchivoService {

    /**
     * Guarda un archivo.
     *
     * @param nombreOriginal nombre del archivo tal como lo envía el cliente
     * @param contenido      origen del contenido; se lee hasta dos veces
     * @return los datos del archivo guardado
     * @throws ReglaNegocioException si el archivo está vacío o su nombre o tipo no están permitidos
     */
    ArchivoAlmacenado guardar(String nombreOriginal, InputStreamSource contenido);

    /**
     * Busca un archivo guardado.
     *
     * @param nombre nombre devuelto por {@link #guardar}
     * @return el archivo
     * @throws RecursoNoEncontradoException si no existe o el nombre no tiene el formato de un archivo guardado
     */
    Resource cargar(String nombre);
}
