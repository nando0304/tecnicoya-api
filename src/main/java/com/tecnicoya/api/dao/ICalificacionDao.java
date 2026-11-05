package com.tecnicoya.api.dao;

import com.tecnicoya.api.model.Calificacion;

/** DAO de {@link Calificacion} (valoración del cliente a un servicio finalizado). */
public interface ICalificacionDao extends IGenericDao<Calificacion, Long> {

    /** Indica si el servicio ya fue calificado. */
    boolean existePorServicio(Long servicioId);

    /** Indica si el servicio tiene otra calificación distinta de {@code idCalificacion}. */
    boolean existePorServicioEnOtraCalificacion(Long servicioId, Long idCalificacion);
}
