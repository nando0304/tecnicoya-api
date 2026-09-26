package com.tecnicoya.api.repository;

import com.tecnicoya.api.entity.Suscripcion;
import com.tecnicoya.api.entity.enums.EstadoSuscripcion;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SuscripcionRepository extends JpaRepository<Suscripcion, Long> {

    @Override
    @EntityGraph(attributePaths = {"tecnico", "tecnico.usuario", "plan"})
    List<Suscripcion> findAll();

    boolean existsByTecnico_IdTecnicoAndEstadoSuscripcion(Long tecnicoId, EstadoSuscripcion estado);

    boolean existsByTecnico_IdTecnicoAndEstadoSuscripcionAndIdSuscripcionNot(Long tecnicoId,
                                                                             EstadoSuscripcion estado,
                                                                             Long idSuscripcion);
}
