package com.tecnicoya.api.dao;

import com.tecnicoya.api.model.enums.EstadoServicio;
import com.tecnicoya.api.model.enums.TipoUsuario;

import java.math.BigDecimal;
import java.util.Map;

public interface IEstadisticasDao {
    Map<EstadoServicio, Long> contarServiciosPorEstado();

    Map<TipoUsuario, Long> contarUsuariosPorTipo();

    BigDecimal sumarPagosAprobados();

    double promedioCalificaciones();
}
