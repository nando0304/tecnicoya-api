package com.tecnicoya.api.repository;

import com.tecnicoya.api.entity.Disponibilidad;
import com.tecnicoya.api.entity.enums.DiaSemana;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalTime;
import java.util.List;

public interface DisponibilidadRepository extends JpaRepository<Disponibilidad, Long> {

    @Override
    @EntityGraph(attributePaths = {"tecnico", "tecnico.usuario"})
    List<Disponibilidad> findAll();

    /** Cuenta las franjas del técnico que se cruzan con [horaInicio, horaFin) el mismo día. */
    @Query("""
            SELECT COUNT(d) FROM Disponibilidad d
            WHERE d.tecnico.idTecnico = :tecnicoId
              AND d.diaSemana = :diaSemana
              AND d.horaInicio < :horaFin
              AND d.horaFin > :horaInicio
              AND (:excluirId IS NULL OR d.idDisponibilidad <> :excluirId)
            """)
    long contarCrucesDeHorario(@Param("tecnicoId") Long tecnicoId,
                               @Param("diaSemana") DiaSemana diaSemana,
                               @Param("horaInicio") LocalTime horaInicio,
                               @Param("horaFin") LocalTime horaFin,
                               @Param("excluirId") Long excluirId);
}
