package com.tecnicoya.api.dao.impl;

import com.tecnicoya.api.dao.IPagoSuscripcionDao;
import com.tecnicoya.api.model.PagoSuscripcion;
import org.springframework.stereotype.Repository;

/** Implementación JPA de {@link IPagoSuscripcionDao}. */
@Repository
public class PagoSuscripcionDaoImpl extends GenericDaoImpl<PagoSuscripcion, Long> implements IPagoSuscripcionDao {

    public PagoSuscripcionDaoImpl() {
        super(PagoSuscripcion.class);
    }

    /** Trae la suscripción y su plan en la misma consulta (evita el problema N+1). */
    @Override
    protected String consultaListar() {
        return """
                SELECT e FROM PagoSuscripcion e
                JOIN FETCH e.suscripcion s
                JOIN FETCH s.plan
                ORDER BY e.idPagoSuscripcion
                """;
    }
}
