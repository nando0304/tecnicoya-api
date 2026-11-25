package com.tecnicoya.api.dao;

import com.tecnicoya.api.model.Suscripcion;
import com.tecnicoya.api.model.enums.EstadoSuscripcion;

public interface ISuscripcionDao extends IGenericDao<Suscripcion, Long> {
    boolean existePorTecnicoYEstado(Long tecnicoId, EstadoSuscripcion estado);

    boolean existePorTecnicoYEstadoEnOtraSuscripcion(Long tecnicoId, EstadoSuscripcion estado, Long idSuscripcion);
}
