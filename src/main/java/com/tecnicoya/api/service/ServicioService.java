package com.tecnicoya.api.service;

import com.tecnicoya.api.dto.request.ServicioRequest;
import com.tecnicoya.api.dto.response.ServicioResponse;

/**
 * Lógica de negocio de servicios solicitados por los clientes.
 *
 * <p>Reglas:
 * <ul>
 *   <li>El cliente debe ser un usuario de tipo {@code CLIENTE} en estado {@code ACTIVO}.</li>
 *   <li>Solo se pueden asignar técnicos {@code VERIFICADO}.</li>
 *   <li>Si no se indica estado queda {@code ASIGNADO} cuando hay técnico y {@code PENDIENTE} cuando no; la prioridad por defecto es {@code MEDIA}.</li>
 *   <li>Los estados {@code ASIGNADO}, {@code EN_PROCESO} y {@code FINALIZADO} exigen un técnico asignado.</li>
 *   <li>La fecha de solicitud la asigna el sistema y la fecha del servicio no puede ser anterior a ella.</li>
 *   <li>La fecha de cierre solo se admite en {@code FINALIZADO} o {@code CANCELADO}; si no se envía, se usa la fecha actual.</li>
 *   <li>No se puede eliminar un servicio con evidencias de pago; su calificación se elimina en cascada.</li>
 * </ul>
 */
public interface ServicioService extends CrudService<ServicioRequest, ServicioResponse> {
}
