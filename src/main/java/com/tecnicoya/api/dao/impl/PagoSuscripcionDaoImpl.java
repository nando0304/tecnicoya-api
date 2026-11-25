package com.tecnicoya.api.dao.impl;

import com.tecnicoya.api.dao.IPagoSuscripcionDao;
import com.tecnicoya.api.model.PagoSuscripcion;
import org.springframework.stereotype.Repository;

@Repository
public class PagoSuscripcionDaoImpl extends GenericDaoImpl<PagoSuscripcion, Long> implements IPagoSuscripcionDao {
    public PagoSuscripcionDaoImpl() {
        super(PagoSuscripcion.class);
    }

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
