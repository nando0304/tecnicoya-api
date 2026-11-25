package com.tecnicoya.api.dao.impl;

import com.tecnicoya.api.dao.IEvidenciaDao;
import com.tecnicoya.api.model.Evidencia;
import org.springframework.stereotype.Repository;

@Repository
public class EvidenciaDaoImpl extends GenericDaoImpl<Evidencia, Long> implements IEvidenciaDao {
    public EvidenciaDaoImpl() {
        super(Evidencia.class);
    }

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
