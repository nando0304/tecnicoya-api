package com.tecnicoya.api.dao;

import com.tecnicoya.api.model.PlanSuscripcion;

public interface IPlanSuscripcionDao extends IGenericDao<PlanSuscripcion, Long> {
    boolean existeNombre(String nombrePlan);

    boolean existeNombreEnOtroPlan(String nombrePlan, Long idPlan);
}
