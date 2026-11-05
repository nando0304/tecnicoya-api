package com.tecnicoya.api.dao.impl;

import com.tecnicoya.api.dao.IEvidenciaPagoDao;
import com.tecnicoya.api.model.EvidenciaPago;
import org.springframework.stereotype.Repository;

/** Implementación JPA de {@link IEvidenciaPagoDao}. */
@Repository
public class EvidenciaPagoDaoImpl extends GenericDaoImpl<EvidenciaPago, Long> implements IEvidenciaPagoDao {

    public EvidenciaPagoDaoImpl() {
        super(EvidenciaPago.class);
    }

    /** Trae el servicio en la misma consulta (evita el problema N+1). */
    @Override
    protected String consultaListar() {
        return """
                SELECT e FROM EvidenciaPago e
                JOIN FETCH e.servicio
                ORDER BY e.idEvidenciaPago
                """;
    }
}
