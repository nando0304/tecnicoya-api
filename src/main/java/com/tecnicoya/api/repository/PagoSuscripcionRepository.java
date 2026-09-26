package com.tecnicoya.api.repository;

import com.tecnicoya.api.entity.PagoSuscripcion;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PagoSuscripcionRepository extends JpaRepository<PagoSuscripcion, Long> {

    @Override
    @EntityGraph(attributePaths = {"suscripcion", "suscripcion.plan"})
    List<PagoSuscripcion> findAll();
}
