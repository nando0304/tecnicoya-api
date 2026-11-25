package com.tecnicoya.api.dao.impl;

import com.tecnicoya.api.dao.ISuscripcionDao;
import com.tecnicoya.api.model.Suscripcion;
import com.tecnicoya.api.model.enums.EstadoSuscripcion;
import org.springframework.stereotype.Repository;

@Repository
public class SuscripcionDaoImpl extends GenericDaoImpl<Suscripcion, Long> implements ISuscripcionDao {
    public SuscripcionDaoImpl() {
        super(Suscripcion.class);
    }

    @Override
    protected String consultaListar() {
        return """
                SELECT e FROM Suscripcion e
                JOIN FETCH e.tecnico t
                JOIN FETCH t.usuario
                JOIN FETCH e.plan
                ORDER BY e.idSuscripcion
                """;
    }

    @Override
    public boolean existePorTecnicoYEstado(Long tecnicoId, EstadoSuscripcion estado) {
        return existe(em.createQuery("""
                        SELECT COUNT(s) FROM Suscripcion s
                        WHERE s.tecnico.idTecnico = :tecnicoId AND s.estadoSuscripcion = :estado
                        """, Long.class)
                .setParameter("tecnicoId", tecnicoId)
                .setParameter("estado", estado));
    }

    @Override
    public boolean existePorTecnicoYEstadoEnOtraSuscripcion(Long tecnicoId, EstadoSuscripcion estado,
                                                           Long idSuscripcion) {
        return existe(em.createQuery("""
                        SELECT COUNT(s) FROM Suscripcion s
                        WHERE s.tecnico.idTecnico = :tecnicoId AND s.estadoSuscripcion = :estado
                          AND s.idSuscripcion <> :idSuscripcion
                        """, Long.class)
                .setParameter("tecnicoId", tecnicoId)
                .setParameter("estado", estado)
                .setParameter("idSuscripcion", idSuscripcion));
    }
}
