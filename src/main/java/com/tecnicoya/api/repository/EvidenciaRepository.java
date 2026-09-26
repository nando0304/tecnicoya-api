package com.tecnicoya.api.repository;

import com.tecnicoya.api.entity.Evidencia;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EvidenciaRepository extends JpaRepository<Evidencia, Long> {

    @Override
    @EntityGraph(attributePaths = {"tecnico", "tecnico.usuario"})
    List<Evidencia> findAll();
}
