package com.tecnicoya.api.repository;

import com.tecnicoya.api.entity.Evidencia;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/** Acceso a datos de {@link Evidencia}. */
public interface EvidenciaRepository extends JpaRepository<Evidencia, Long> {

    /** Lista todos los registros cargando el técnico en la misma consulta (evita el problema N+1). */
    @Override
    @EntityGraph(attributePaths = {"tecnico", "tecnico.usuario"})
    List<Evidencia> findAll();
}
