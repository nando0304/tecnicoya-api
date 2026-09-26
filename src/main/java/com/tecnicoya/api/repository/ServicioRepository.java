package com.tecnicoya.api.repository;

import com.tecnicoya.api.entity.Servicio;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ServicioRepository extends JpaRepository<Servicio, Long> {

    @Override
    @EntityGraph(attributePaths = {"cliente", "tecnico", "tecnico.usuario"})
    List<Servicio> findAll();
}
