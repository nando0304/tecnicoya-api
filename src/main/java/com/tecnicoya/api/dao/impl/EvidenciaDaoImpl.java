package com.tecnicoya.api.dao.impl;

import com.tecnicoya.api.dao.IEvidenciaDao;
import com.tecnicoya.api.model.Evidencia;
import org.springframework.stereotype.Repository;

/** Implementación JPA de {@link IEvidenciaDao}. */
@Repository
public class EvidenciaDaoImpl extends GenericDaoImpl<Evidencia, Long> implements IEvidenciaDao {

    public EvidenciaDaoImpl() {
        super(Evidencia.class);
    }

    /** Trae el técnico y su usuario en la misma consulta (evita el problema N+1). */
    @Override
    protected String consultaListar() {
        return """
                SELECT e FROM Evidencia e
                JOIN FETCH e.tecnico t
                JOIN FETCH t.usuario
                ORDER BY e.idEvidencia
                """;
    }
}
