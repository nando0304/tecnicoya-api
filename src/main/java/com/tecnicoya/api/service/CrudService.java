package com.tecnicoya.api.service;

import java.util.List;

/**
 * Operaciones CRUD comunes a todos los recursos de la API.
 *
 * @param <Q> DTO de entrada (request)
 * @param <R> DTO de salida (response)
 */
public interface CrudService<Q, R> {

    List<R> listar();

    R obtenerPorId(Long id);

    R crear(Q request);

    R actualizar(Long id, Q request);

    void eliminar(Long id);
}
