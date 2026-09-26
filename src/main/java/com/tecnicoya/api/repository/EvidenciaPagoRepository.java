package com.tecnicoya.api.repository;

import com.tecnicoya.api.entity.EvidenciaPago;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EvidenciaPagoRepository extends JpaRepository<EvidenciaPago, Long> {

    @Override
    @EntityGraph(attributePaths = "servicio")
    List<EvidenciaPago> findAll();
}
