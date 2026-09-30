package com.tecnicoya.api.service;

import com.tecnicoya.api.exception.ConflictoException;
import com.tecnicoya.api.exception.RecursoNoEncontradoException;
import com.tecnicoya.api.exception.ReglaNegocioException;

import java.util.List;

/**
 * Operaciones CRUD comunes a todos los recursos de la API.
 *
 * <p>Cada recurso define su propia interfaz (por ejemplo {@link UsuarioService}) que extiende esta,
 * documenta sus reglas de negocio y puede añadir operaciones propias.
 *
 * @param <Q> DTO de entrada (request)
 * @param <R> DTO de salida (response)
 */
public interface CrudService<Q, R> {

    /**
     * Lista todos los registros del recurso.
     *
     * @return los registros; lista vacía si no hay ninguno
     */
    List<R> listar();

    /**
     * Busca un registro por su id.
     *
     * @param id identificador del registro
     * @return el registro encontrado
     * @throws RecursoNoEncontradoException si no existe
     */
    R obtenerPorId(Long id);

    /**
     * Registra un nuevo registro aplicando las reglas de negocio del recurso.
     *
     * @param request datos del nuevo registro, ya validados con Bean Validation
     * @return el registro creado, con su id
     * @throws RecursoNoEncontradoException si hace referencia a otro registro que no existe
     * @throws ReglaNegocioException        si incumple una regla de negocio
     * @throws ConflictoException           si duplica un dato único o choca con otro registro
     */
    R crear(Q request);

    /**
     * Reemplaza los datos de un registro existente.
     *
     * <p>Los campos de estado opcionales que llegan nulos conservan su valor actual.
     *
     * @param id      identificador del registro
     * @param request nuevos datos, ya validados con Bean Validation
     * @return el registro actualizado
     * @throws RecursoNoEncontradoException si el registro, o uno al que hace referencia, no existe
     * @throws ReglaNegocioException        si incumple una regla de negocio
     * @throws ConflictoException           si duplica un dato único o choca con otro registro
     */
    R actualizar(Long id, Q request);

    /**
     * Elimina un registro.
     *
     * @param id identificador del registro
     * @throws RecursoNoEncontradoException si no existe
     * @throws org.springframework.dao.DataIntegrityViolationException si otros registros dependen de él
     */
    void eliminar(Long id);
}
