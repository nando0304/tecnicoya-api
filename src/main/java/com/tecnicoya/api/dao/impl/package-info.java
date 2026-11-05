/**
 * Implementaciones JPA de los DAO. Todas extienden {@link com.tecnicoya.api.dao.impl.GenericDaoImpl} (salvo el DAO de
 * estadísticas, que no administra una entidad) y se anotan con {@code @Repository} para que Spring traduzca las
 * excepciones de persistencia.
 *
 * <p>Todas las consultas usan parámetros nombrados ({@code :correo}, {@code :idUsuario}…): los valores que llegan
 * del usuario nunca se concatenan en el texto de la consulta, lo que previene la inyección SQL.
 */
package com.tecnicoya.api.dao.impl;
