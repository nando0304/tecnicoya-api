package com.tecnicoya.api.repository;

import com.tecnicoya.api.entity.Disponibilidad;
import com.tecnicoya.api.entity.enums.DiaSemana;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalTime;
import java.util.List;

/** Acceso a datos de {@link Disponibilidad}. */
public interface DisponibilidadRepository extends JpaRepository<Disponibilidad, Long> {

    /** Lista todos los registros cargando el técnico en la misma consulta (evita el problema N+1). */
    @Override
    @EntityGraph(attributePaths = {"tecnico", "tecnico.usuario"})
    List<Disponibilidad> findAll();

    /**
     * Cuenta las franjas del técnico que se cruzan con {@code [horaInicio, horaFin)} el mismo día.
     *
     * @param tecnicoId  técnico dueño de las franjas
     * @param diaSemana  día a revisar
     * @param horaInicio inicio de la franja nueva
     * @param horaFin    fin de la franja nueva
     * @param excluirId  franja que se está actualizando, para no compararla consigo misma;
     *                   {@code null} al crear
     * @return cantidad de franjas que se cruzan
     */
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
