package com.tecnicoya.api.service;

import com.tecnicoya.api.dto.request.PagoSuscripcionRequest;
import com.tecnicoya.api.dto.response.PagoSuscripcionResponse;

/**
 * Lógica de negocio de pagos de suscripción.
 *
 * <p>Reglas:
 * <ul>
 *   <li>No se registran pagos para suscripciones {@code CANCELADA}.</li>
 *   <li>El monto debe ser mayor que 0, con hasta 2 decimales.</li>
 *   <li>Si no se envía la fecha de pago se usa la actual; el estado por defecto es {@code PENDIENTE}.</li>
 * </ul>
 */
public interface PagoSuscripcionService extends CrudService<PagoSuscripcionRequest, PagoSuscripcionResponse> {
}
