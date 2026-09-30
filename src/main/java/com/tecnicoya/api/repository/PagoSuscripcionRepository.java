package com.tecnicoya.api.repository;

import com.tecnicoya.api.entity.PagoSuscripcion;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/** Acceso a datos de {@link PagoSuscripcion}. */
public interface PagoSuscripcionRepository extends JpaRepository<PagoSuscripcion, Long> {

    /** Lista todos los registros cargando la suscripción y su plan en la misma consulta (evita el problema N+1). */
    @Override
    @EntityGraph(attributePaths = {"suscripcion", "suscripcion.plan"})
    List<PagoSuscripcion> findAll();
}
