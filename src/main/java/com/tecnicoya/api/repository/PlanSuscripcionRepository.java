package com.tecnicoya.api.repository;

import com.tecnicoya.api.entity.PlanSuscripcion;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlanSuscripcionRepository extends JpaRepository<PlanSuscripcion, Long> {

    boolean existsByNombrePlanIgnoreCase(String nombrePlan);

    boolean existsByNombrePlanIgnoreCaseAndIdPlanNot(String nombrePlan, Long idPlan);
}
