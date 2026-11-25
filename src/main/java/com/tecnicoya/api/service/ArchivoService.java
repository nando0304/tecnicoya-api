package com.tecnicoya.api.service;

import org.springframework.core.io.InputStreamSource;
import org.springframework.core.io.Resource;

public interface ArchivoService {
    ArchivoAlmacenado guardar(String nombreOriginal, InputStreamSource contenido);

    Resource cargar(String nombre);
}
