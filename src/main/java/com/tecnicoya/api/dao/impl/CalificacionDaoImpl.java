package com.tecnicoya.api.dao.impl;

import com.tecnicoya.api.dao.ICalificacionDao;
import com.tecnicoya.api.model.Calificacion;
import org.springframework.stereotype.Repository;

@Repository
public class CalificacionDaoImpl extends GenericDaoImpl<Calificacion, Long> implements ICalificacionDao {
    public CalificacionDaoImpl() {
        super(Calificacion.class);
    }

    @Override
    protected String consultaListar() {
        return """
                SELECT e FROM Calificacion e
                JOIN FETCH e.servicio s
                LEFT JOIN FETCH s.tecnico t
                LEFT JOIN FETCH t.usuario
                ORDER BY e.idCalificacion
                """;
    }

    @Override
    public boolean existePorServicio(Long servicioId) {
        return existe(em.createQuery(
                        "SELECT COUNT(c) FROM Calificacion c WHERE c.servicio.idServicio = :servicioId", Long.class)
                .setParameter("servicioId", servicioId));
    }

    @Override
    public boolean existePorServicioEnOtraCalificacion(Long servicioId, Long idCalificacion) {
        return existe(em.createQuery("""
                        SELECT COUNT(c) FROM Calificacion c
                        WHERE c.servicio.idServicio = :servicioId AND c.idCalificacion <> :idCalificacion
                        """, Long.class)
                .setParameter("servicioId", servicioId)
                .setParameter("idCalificacion", idCalificacion));
    }
}
