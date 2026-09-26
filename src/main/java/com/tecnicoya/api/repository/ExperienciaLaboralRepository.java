package com.tecnicoya.api.repository;

import com.tecnicoya.api.entity.ExperienciaLaboral;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExperienciaLaboralRepository extends JpaRepository<ExperienciaLaboral, Long> {

    @Override
    @EntityGraph(attributePaths = {"tecnico", "tecnico.usuario"})
    List<ExperienciaLaboral> findAll();
}
