package com.tecnicoya.api.service;

import com.tecnicoya.api.dto.request.TecnicoRequest;
import com.tecnicoya.api.dto.response.TecnicoResponse;

/**
 * Lógica de negocio de perfiles de técnico.
 *
 * <p>Reglas:
 * <ul>
 *   <li>El usuario debe existir y ser de tipo {@code TECNICO}.</li>
 *   <li>Cada usuario tiene como máximo un perfil de técnico.</li>
 *   <li>El estado de verificación por defecto es {@code PENDIENTE}; al actualizar, si no se envía, se conserva.</li>
 *   <li>No se puede eliminar un técnico con servicios o suscripciones; su disponibilidad, evidencias y experiencia se eliminan en cascada.</li>
 * </ul>
 */
public interface TecnicoService extends CrudService<TecnicoRequest, TecnicoResponse> {
}
