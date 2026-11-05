package com.tecnicoya.api.dao;

import com.tecnicoya.api.model.Suscripcion;
import com.tecnicoya.api.model.enums.EstadoSuscripcion;

/** DAO de {@link Suscripcion} (plan contratado por un técnico). */
public interface ISuscripcionDao extends IGenericDao<Suscripcion, Long> {

    /** Indica si el técnico tiene alguna suscripción en el estado indicado. */
    boolean existePorTecnicoYEstado(Long tecnicoId, EstadoSuscripcion estado);

    /** Igual que el anterior, pero sin contar la suscripción {@code idSuscripcion} (validación al actualizar). */
    boolean existePorTecnicoYEstadoEnOtraSuscripcion(Long tecnicoId, EstadoSuscripcion estado, Long idSuscripcion);
}
