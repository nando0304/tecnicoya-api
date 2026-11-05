/**
 * Capa de acceso a datos con el patrón DAO (Data Access Object).
 *
 * <p>Este paquete contiene solo los contratos: {@link com.tecnicoya.api.dao.IGenericDao} con el CRUD común y una
 * interfaz {@code IXxxDao} por entidad con sus consultas propias. Las implementaciones están en
 * {@link com.tecnicoya.api.dao.impl} y usan JPA ({@code EntityManager}) con JPQL parametrizado.
 *
 * <p>Los servicios dependen únicamente de las interfaces, por lo que la tecnología de persistencia puede cambiar
 * (JDBC, otro ORM) sin modificar la lógica de negocio.
 */
package com.tecnicoya.api.dao;
