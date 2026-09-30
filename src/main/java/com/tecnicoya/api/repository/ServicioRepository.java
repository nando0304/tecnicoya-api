package com.tecnicoya.api.repository;

import com.tecnicoya.api.entity.Servicio;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/** Acceso a datos de {@link Servicio}. */
public interface ServicioRepository extends JpaRepository<Servicio, Long> {

    /** Lista todos los registros cargando el cliente y el técnico en la misma consulta (evita el problema N+1). */
    @Override
    @EntityGraph(attributePaths = {"cliente", "tecnico", "tecnico.usuario"})
    List<Servicio> findAll();
}
