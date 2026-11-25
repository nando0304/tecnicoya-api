package com.tecnicoya.api.dao;

import com.tecnicoya.api.model.Usuario;

import java.util.Optional;

public interface IUsuarioDao extends IGenericDao<Usuario, Long> {
    Optional<Usuario> buscarPorCorreo(String correo);

    boolean existeCorreo(String correo);

    boolean existeCorreoEnOtroUsuario(String correo, Long idUsuario);
}
