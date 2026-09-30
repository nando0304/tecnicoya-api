package com.tecnicoya.api.repository;

import com.tecnicoya.api.entity.Suscripcion;
import com.tecnicoya.api.entity.enums.EstadoSuscripcion;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/** Acceso a datos de {@link Suscripcion}. */
public interface SuscripcionRepository extends JpaRepository<Suscripcion, Long> {

    /** Lista todos los registros cargando el técnico y el plan en la misma consulta (evita el problema N+1). */
    @Override
    @EntityGraph(attributePaths = {"tecnico", "tecnico.usuario", "plan"})
    List<Suscripcion> findAll();

    /** Indica si el técnico tiene alguna suscripción en el estado indicado. */
    boolean existsByTecnico_IdTecnicoAndEstadoSuscripcion(Long tecnicoId, EstadoSuscripcion estado);

    /** Igual que el anterior, pero sin contar la suscripción {@code idSuscripcion} (validación al actualizar). */
    boolean existsByTecnico_IdTecnicoAndEstadoSuscripcionAndIdSuscripcionNot(Long tecnicoId,
                                                                             EstadoSuscripcion estado,
                                                                             Long idSuscripcion);
}
