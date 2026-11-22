/**
 * Monitoreo (observabilidad) de la API: id de correlación de cada petición
 * ({@link com.tecnicoya.api.monitoreo.IdPeticionFilter}), métricas del sistema operativo
 * ({@link com.tecnicoya.api.monitoreo.MetricasSistemaOperativo}) y rastreo de errores del front-end
 * ({@link com.tecnicoya.api.monitoreo.RegistroErroresFrontend}).
 *
 * <p>El resto de las métricas (tiempos de respuesta, errores HTTP, JVM, base de datos, disco) las publica Spring Boot
 * Actuator con Micrometer en {@code /actuator}, protegido por
 * {@link com.tecnicoya.api.security.MonitoreoSeguridadFilter}. Guía completa en {@code docs/monitoreo.md}.
 */
package com.tecnicoya.api.monitoreo;
