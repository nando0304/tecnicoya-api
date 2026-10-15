package com.tecnicoya.api.service;

/**
 * Archivo guardado por {@link ArchivoService}.
 *
 * @param nombre         nombre con el que quedó guardado: su huella SHA-256 y su extensión
 * @param nombreOriginal nombre que envió el cliente, sin la ruta
 * @param tamanoBytes    tamaño del contenido
 * @param sha256         huella SHA-256 del contenido, en hexadecimal
 */
public record ArchivoAlmacenado(String nombre, String nombreOriginal, long tamanoBytes, String sha256) {
}
