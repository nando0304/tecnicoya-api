package com.tecnicoya.api.dao.impl;

import com.tecnicoya.api.dao.IPlanSuscripcionDao;
import com.tecnicoya.api.model.PlanSuscripcion;
import org.springframework.stereotype.Repository;

@Repository
public class PlanSuscripcionDaoImpl extends GenericDaoImpl<PlanSuscripcion, Long> implements IPlanSuscripcionDao {
    public PlanSuscripcionDaoImpl() {
        super(PlanSuscripcion.class);
    }

    @Override
    public boolean existeNombre(String nombrePlan) {
        return existe(em.createQuery(
                        "SELECT COUNT(p) FROM PlanSuscripcion p WHERE LOWER(p.nombrePlan) = LOWER(:nombre)", Long.class)
                .setParameter("nombre", nombrePlan));
    }

    @Override
    public boolean existeNombreEnOtroPlan(String nombrePlan, Long idPlan) {
        return existe(em.createQuery("""
                        SELECT COUNT(p) FROM PlanSuscripcion p
                        WHERE LOWER(p.nombrePlan) = LOWER(:nombre) AND p.idPlan <> :idPlan
                        """, Long.class)
                .setParameter("nombre", nombrePlan)
                .setParameter("idPlan", idPlan));
    }
}
