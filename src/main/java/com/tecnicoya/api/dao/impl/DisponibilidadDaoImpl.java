package com.tecnicoya.api.dao.impl;

import com.tecnicoya.api.dao.IDisponibilidadDao;
import com.tecnicoya.api.model.Disponibilidad;
import com.tecnicoya.api.model.enums.DiaSemana;
import org.springframework.stereotype.Repository;

import java.time.LocalTime;

@Repository
public class DisponibilidadDaoImpl extends GenericDaoImpl<Disponibilidad, Long> implements IDisponibilidadDao {
    public DisponibilidadDaoImpl() {
        super(Disponibilidad.class);
    }

    @Override
    protected String consultaListar() {
        return """
                SELECT e FROM Disponibilidad e
                JOIN FETCH e.tecnico t
                JOIN FETCH t.usuario
                ORDER BY e.idDisponibilidad
                """;
    }

    @Override
    public long contarCrucesDeHorario(Long tecnicoId, DiaSemana diaSemana, LocalTime horaInicio, LocalTime horaFin,
                                      Long excluirId) {
        return em.createQuery("""
                        SELECT COUNT(d) FROM Disponibilidad d
                        WHERE d.tecnico.idTecnico = :tecnicoId
                          AND d.diaSemana = :diaSemana
                          AND d.horaInicio < :horaFin
                          AND d.horaFin > :horaInicio
                          AND (:excluirId IS NULL OR d.idDisponibilidad <> :excluirId)
                        """, Long.class)
                .setParameter("tecnicoId", tecnicoId)
                .setParameter("diaSemana", diaSemana)
                .setParameter("horaInicio", horaInicio)
                .setParameter("horaFin", horaFin)
                .setParameter("excluirId", excluirId)
                .getSingleResult();
    }
}
