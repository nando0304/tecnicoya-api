package com.tecnicoya.api.repository;

import com.tecnicoya.api.entity.Calificacion;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CalificacionRepository extends JpaRepository<Calificacion, Long> {

    @Override
    @EntityGraph(attributePaths = {"servicio", "servicio.tecnico", "servicio.tecnico.usuario"})
    List<Calificacion> findAll();

    boolean existsByServicio_IdServicio(Long servicioId);

    boolean existsByServicio_IdServicioAndIdCalificacionNot(Long servicioId, Long idCalificacion);
}
