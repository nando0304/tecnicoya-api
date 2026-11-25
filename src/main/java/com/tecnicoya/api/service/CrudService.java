package com.tecnicoya.api.service;

import java.util.List;

public interface CrudService<Q, R> {
    List<R> listar();

    R obtenerPorId(Long id);

    R crear(Q request);

    R actualizar(Long id, Q request);

    void eliminar(Long id);
}
