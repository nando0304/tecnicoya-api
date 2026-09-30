package com.tecnicoya.api.repository;

import com.tecnicoya.api.entity.Calificacion;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/** Acceso a datos de {@link Calificacion}. */
public interface CalificacionRepository extends JpaRepository<Calificacion, Long> {

    /** Lista todos los registros cargando el servicio y su técnico en la misma consulta (evita el problema N+1). */
    @Override
    @EntityGraph(attributePaths = {"servicio", "servicio.tecnico", "servicio.tecnico.usuario"})
    List<Calificacion> findAll();

    /** Indica si el servicio ya fue calificado. */
    boolean existsByServicio_IdServicio(Long servicioId);

    /** Indica si el servicio tiene otra calificación distinta de {@code idCalificacion}. */
    boolean existsByServicio_IdServicioAndIdCalificacionNot(Long servicioId, Long idCalificacion);
}
