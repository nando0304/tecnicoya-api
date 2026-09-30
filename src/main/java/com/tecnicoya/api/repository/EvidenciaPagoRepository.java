package com.tecnicoya.api.repository;

import com.tecnicoya.api.entity.EvidenciaPago;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/** Acceso a datos de {@link EvidenciaPago}. */
public interface EvidenciaPagoRepository extends JpaRepository<EvidenciaPago, Long> {

    /** Lista todos los registros cargando el servicio en la misma consulta (evita el problema N+1). */
    @Override
    @EntityGraph(attributePaths = "servicio")
    List<EvidenciaPago> findAll();
}
