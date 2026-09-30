package com.tecnicoya.api.dto.response;

import com.tecnicoya.api.entity.PlanSuscripcion;
import com.tecnicoya.api.entity.enums.EstadoRegistro;

import java.math.BigDecimal;

/** Plan de suscripción devuelto por la API. */
public record PlanSuscripcionResponse(
        Long idPlan,
        String nombrePlan,
        BigDecimal precioInicial,
        Integer limiteClientes,
        BigDecimal precioPosterior,
        String descripcion,
        EstadoRegistro estado
) {
    public static PlanSuscripcionResponse desde(PlanSuscripcion plan) {
        return new PlanSuscripcionResponse(
                plan.getIdPlan(),
                plan.getNombrePlan(),
                plan.getPrecioInicial(),
                plan.getLimiteClientes(),
                plan.getPrecioPosterior(),
                plan.getDescripcion(),
                plan.getEstado());
    }
}
