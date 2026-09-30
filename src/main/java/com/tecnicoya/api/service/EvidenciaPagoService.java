package com.tecnicoya.api.service;

import com.tecnicoya.api.dto.request.EvidenciaPagoRequest;
import com.tecnicoya.api.dto.response.EvidenciaPagoResponse;

/**
 * Lógica de negocio de evidencias de pago de servicios.
 *
 * <p>Reglas:
 * <ul>
 *   <li>El servicio no puede estar {@code CANCELADO} y debe tener un técnico asignado.</li>
 *   <li>El monto debe ser mayor que 0, con hasta 2 decimales.</li>
 *   <li>Si no se envía la fecha de pago se usa la actual (no puede ser futura); el estado de validación por defecto es {@code PENDIENTE}.</li>
 * </ul>
 */
public interface EvidenciaPagoService extends CrudService<EvidenciaPagoRequest, EvidenciaPagoResponse> {
}
