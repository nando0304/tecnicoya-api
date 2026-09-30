package com.tecnicoya.api.service;

import com.tecnicoya.api.dto.request.ExperienciaLaboralRequest;
import com.tecnicoya.api.dto.response.ExperienciaLaboralResponse;

/**
 * Lógica de negocio de experiencia laboral de los técnicos.
 *
 * <p>Reglas:
 * <ul>
 *   <li>Si {@code actualidad} es {@code true} no lleva fecha de fin; si es {@code false}, la fecha de fin es obligatoria.</li>
 *   <li>La fecha de fin no puede ser anterior a la de inicio, y ninguna de las dos puede ser futura.</li>
 * </ul>
 */
public interface ExperienciaLaboralService extends CrudService<ExperienciaLaboralRequest, ExperienciaLaboralResponse> {
}
