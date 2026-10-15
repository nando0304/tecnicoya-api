/**
 * API REST de TécnicoYa: plataforma que conecta clientes con técnicos de servicios a domicilio.
 *
 * <p>Arquitectura MVC en capas: {@code controller} (Controlador) → {@code service} → {@code repository}
 * → {@code model} (Modelo), con DTOs en {@code dto} para no exponer las entidades. La Vista es el front-end,
 * que consume las respuestas JSON.
 */
package com.tecnicoya.api;
