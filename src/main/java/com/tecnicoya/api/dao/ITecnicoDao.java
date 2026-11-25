package com.tecnicoya.api.dao;

import com.tecnicoya.api.model.Tecnico;

public interface ITecnicoDao extends IGenericDao<Tecnico, Long> {
    boolean existePerfilDeUsuario(Long usuarioId);

    boolean existePerfilDeUsuarioEnOtroTecnico(Long usuarioId, Long idTecnico);
}
