package com.tecnicoya.api.dao;

import com.tecnicoya.api.model.Calificacion;

public interface ICalificacionDao extends IGenericDao<Calificacion, Long> {
    boolean existePorServicio(Long servicioId);

    boolean existePorServicioEnOtraCalificacion(Long servicioId, Long idCalificacion);
}
