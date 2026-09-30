package com.tecnicoya.api.service;

import com.tecnicoya.api.dto.request.SuscripcionRequest;
import com.tecnicoya.api.dto.response.SuscripcionResponse;

/**
 * Lógica de negocio de suscripciones de técnicos a planes.
 *
 * <p>Reglas:
 * <ul>
 *   <li>La fecha de fin debe ser posterior a la de inicio.</li>
 *   <li>El plan debe estar {@code ACTIVO} al contratarlo o al cambiarlo.</li>
 *   <li>Un técnico solo puede tener una suscripción {@code ACTIVA}.</li>
 *   <li>La fecha de cancelación solo se admite en {@code CANCELADA}; si no se envía, se usa la fecha actual.</li>
 *   <li>El estado por defecto es {@code PENDIENTE}.</li>
 *   <li>No se puede eliminar una suscripción que tenga pagos.</li>
 * </ul>
 */
public interface SuscripcionService extends CrudService<SuscripcionRequest, SuscripcionResponse> {
}
