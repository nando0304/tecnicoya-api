package com.tecnicoya.api.dao;

import java.util.List;
import java.util.Optional;

public interface IGenericDao<T, ID> {
    List<T> listar();

    Optional<T> buscarPorId(ID id);

    T guardar(T entidad);

    void eliminar(T entidad);

    long contar();
}
