package com.tecnicoya.api.dao;

import com.tecnicoya.api.model.Tecnico;

/** DAO de {@link Tecnico} (perfil profesional de un usuario de tipo TECNICO). */
public interface ITecnicoDao extends IGenericDao<Tecnico, Long> {

    /** Indica si el usuario ya tiene un perfil de técnico. */
    boolean existePerfilDeUsuario(Long usuarioId);

    /** Indica si el usuario tiene otro perfil de técnico distinto de {@code idTecnico}. */
    boolean existePerfilDeUsuarioEnOtroTecnico(Long usuarioId, Long idTecnico);
}
