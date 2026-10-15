package com.tecnicoya.api.dto.response;

import com.tecnicoya.api.service.ArchivoAlmacenado;

import java.net.URI;

/**
 * Archivo subido. La {@code url} es la que se envía después en {@code urlArchivo} (evidencias)
 * o en {@code archivoEvidencia} (evidencias de pago).
 */
public record ArchivoResponse(
        String url,
        String nombre,
        String nombreOriginal,
        long tamanoBytes,
        String sha256
) {
    public static ArchivoResponse desde(ArchivoAlmacenado archivo, URI url) {
        return new ArchivoResponse(
                url.toString(),
                archivo.nombre(),
                archivo.nombreOriginal(),
                archivo.tamanoBytes(),
                archivo.sha256());
    }
}
