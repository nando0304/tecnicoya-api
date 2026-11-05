package com.tecnicoya.api.dao;

import java.util.List;
import java.util.Optional;

/**
 * Contrato del patrón DAO (Data Access Object): operaciones básicas de persistencia comunes a todas las entidades.
 *
 * <p>Los servicios solo conocen estas interfaces ({@code IUsuarioDao}, {@code IServicioDao}…), nunca la
 * tecnología de acceso a datos. Las implementaciones del paquete {@code dao.impl} usan JPA ({@code EntityManager})
 * y consultas JPQL con parámetros nombrados, lo que evita la inyección SQL.
 *
 * @param <T>  entidad que administra el DAO
 * @param <ID> tipo de su identificador
 */
public interface IGenericDao<T, ID> {

    /**
     * Lista todos los registros, ordenados por su id.
     *
     * @return los registros; lista vacía si no hay ninguno
     */
    List<T> listar();

    /**
     * Busca un registro por su id.
     *
     * @param id identificador; no puede ser {@code null}
     * @return el registro, o vacío si no existe
     */
    Optional<T> buscarPorId(ID id);

    /**
     * Inserta la entidad si aún no tiene id, o actualiza sus datos si ya existe.
     *
     * @param entidad entidad a guardar; no puede ser {@code null}
     * @return la entidad guardada (con su id generado, si era nueva)
     */
    T guardar(T entidad);

    /**
     * Elimina el registro y sincroniza de inmediato con la base de datos, para que una restricción de
     * integridad (otros registros que dependen de él) se detecte dentro de la misma operación.
     *
     * @param entidad entidad a eliminar
     * @throws org.springframework.dao.DataIntegrityViolationException si otros registros dependen de él
     */
    void eliminar(T entidad);

    /**
     * Cuenta los registros de la entidad.
     *
     * @return cantidad de registros
     */
    long contar();
}
