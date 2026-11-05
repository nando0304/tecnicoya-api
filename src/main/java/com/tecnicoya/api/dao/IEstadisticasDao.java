package com.tecnicoya.api.dao;

import com.tecnicoya.api.model.enums.EstadoServicio;
import com.tecnicoya.api.model.enums.TipoUsuario;

import java.math.BigDecimal;
import java.util.Map;

/**
 * DAO de solo lectura con consultas agregadas ({@code COUNT}, {@code SUM}, {@code AVG}) para los reportes.
 *
 * <p>A diferencia de los demás DAO no administra una entidad: calcula los indicadores directamente en la base de
 * datos, sin traer todos los registros a memoria.
 */
public interface IEstadisticasDao {

    /**
     * Cuenta los servicios agrupados por estado.
     *
     * @return mapa inmutable con todos los estados, en el orden del enum; 0 si un estado no tiene servicios
     */
    Map<EstadoServicio, Long> contarServiciosPorEstado();

    /**
     * Cuenta los usuarios agrupados por tipo.
     *
     * @return mapa inmutable con todos los tipos, en el orden del enum; 0 si un tipo no tiene usuarios
     */
    Map<TipoUsuario, Long> contarUsuariosPorTipo();

    /**
     * Suma el monto de los pagos de suscripción {@code APROBADO}.
     *
     * @return total recaudado; {@code 0} si no hay pagos aprobados
     */
    BigDecimal sumarPagosAprobados();

    /**
     * Promedio de las puntuaciones de todas las calificaciones.
     *
     * @return promedio entre 1 y 5; {@code 0} si aún no hay calificaciones
     */
    double promedioCalificaciones();
}
