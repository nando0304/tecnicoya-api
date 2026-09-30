package com.tecnicoya.api.service;

import com.tecnicoya.api.dto.request.PlanSuscripcionRequest;
import com.tecnicoya.api.dto.response.PlanSuscripcionResponse;

/**
 * Lógica de negocio de planes de suscripción.
 *
 * <p>Reglas:
 * <ul>
 *   <li>El nombre del plan es único, sin distinguir mayúsculas de minúsculas.</li>
 *   <li>Los precios no pueden ser negativos y el límite de clientes debe ser al menos 1.</li>
 *   <li>El estado por defecto es {@code ACTIVO}.</li>
 *   <li>No se puede eliminar un plan que tenga suscripciones.</li>
 * </ul>
 */
public interface PlanSuscripcionService extends CrudService<PlanSuscripcionRequest, PlanSuscripcionResponse> {
}
