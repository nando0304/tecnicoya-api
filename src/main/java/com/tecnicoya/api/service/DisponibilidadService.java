package com.tecnicoya.api.service;

import com.tecnicoya.api.dto.request.DisponibilidadRequest;
import com.tecnicoya.api.dto.response.DisponibilidadResponse;

/**
 * Lógica de negocio de franjas de disponibilidad de los técnicos.
 *
 * <p>Reglas:
 * <ul>
 *   <li>La hora de fin debe ser posterior a la hora de inicio.</li>
 *   <li>Una franja no puede cruzarse con otra del mismo técnico en el mismo día.</li>
 *   <li>El estado por defecto es {@code ACTIVO}.</li>
 * </ul>
 */
public interface DisponibilidadService extends CrudService<DisponibilidadRequest, DisponibilidadResponse> {
}
