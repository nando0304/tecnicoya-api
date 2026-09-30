package com.tecnicoya.api.repository;

import com.tecnicoya.api.entity.PlanSuscripcion;
import org.springframework.data.jpa.repository.JpaRepository;

/** Acceso a datos de {@link PlanSuscripcion}. */
public interface PlanSuscripcionRepository extends JpaRepository<PlanSuscripcion, Long> {

    /** Indica si ya existe un plan con ese nombre, sin distinguir mayúsculas. */
    boolean existsByNombrePlanIgnoreCase(String nombrePlan);

    /** Indica si otro plan, distinto de {@code idPlan}, ya usa ese nombre. */
    boolean existsByNombrePlanIgnoreCaseAndIdPlanNot(String nombrePlan, Long idPlan);
}
