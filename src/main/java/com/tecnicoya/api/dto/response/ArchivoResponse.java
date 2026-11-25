package com.tecnicoya.api.dto.response;

import com.tecnicoya.api.service.ArchivoAlmacenado;

import java.net.URI;

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
