package com.tecnicoya.api.dao;

import com.tecnicoya.api.model.Usuario;

import java.util.Optional;

/** DAO de {@link Usuario} (clientes, técnicos y administradores). */
public interface IUsuarioDao extends IGenericDao<Usuario, Long> {

    /**
     * Busca un usuario por su correo.
     *
     * @param correo correo normalizado en minúsculas
     * @return el usuario, o vacío si no existe
     */
    Optional<Usuario> buscarPorCorreo(String correo);

    /** Indica si algún usuario ya usa el correo. */
    boolean existeCorreo(String correo);

    /** Indica si otro usuario, distinto de {@code idUsuario}, ya usa el correo (validación al actualizar). */
    boolean existeCorreoEnOtroUsuario(String correo, Long idUsuario);
}
