package com.tecnicoya.api.dao;

import com.tecnicoya.api.model.PlanSuscripcion;

/** DAO de {@link PlanSuscripcion} (planes que pueden contratar los técnicos). */
public interface IPlanSuscripcionDao extends IGenericDao<PlanSuscripcion, Long> {

    /** Indica si ya existe un plan con ese nombre, sin distinguir mayúsculas. */
    boolean existeNombre(String nombrePlan);

    /** Indica si otro plan, distinto de {@code idPlan}, ya usa ese nombre (sin distinguir mayúsculas). */
    boolean existeNombreEnOtroPlan(String nombrePlan, Long idPlan);
}
