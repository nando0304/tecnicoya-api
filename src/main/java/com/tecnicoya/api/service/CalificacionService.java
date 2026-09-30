package com.tecnicoya.api.service;

import com.tecnicoya.api.dto.request.CalificacionRequest;
import com.tecnicoya.api.dto.response.CalificacionResponse;

/**
 * Lógica de negocio de calificaciones de servicios.
 *
 * <p>Reglas:
 * <ul>
 *   <li>Solo se pueden calificar servicios {@code FINALIZADO}.</li>
 *   <li>Cada servicio admite como máximo una calificación.</li>
 *   <li>La puntuación va de 1 a 5 y la fecha de calificación la asigna el sistema.</li>
 * </ul>
 */
public interface CalificacionService extends CrudService<CalificacionRequest, CalificacionResponse> {
}
