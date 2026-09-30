package com.tecnicoya.api.service;

import com.tecnicoya.api.dto.request.EvidenciaRequest;
import com.tecnicoya.api.dto.response.EvidenciaResponse;

/**
 * Lógica de negocio de evidencias (documentos) de los técnicos.
 *
 * <p>Reglas:
 * <ul>
 *   <li>La URL del archivo debe empezar por {@code http://} o {@code https://}.</li>
 *   <li>El estado de validación por defecto es {@code PENDIENTE}.</li>
 *   <li>La fecha de carga la asigna el sistema.</li>
 * </ul>
 */
public interface EvidenciaService extends CrudService<EvidenciaRequest, EvidenciaResponse> {
}
