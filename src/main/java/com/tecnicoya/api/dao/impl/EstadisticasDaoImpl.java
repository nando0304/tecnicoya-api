package com.tecnicoya.api.dao.impl;

import com.google.common.collect.Maps;
import com.tecnicoya.api.dao.IEstadisticasDao;
import com.tecnicoya.api.model.enums.EstadoPago;
import com.tecnicoya.api.model.enums.EstadoServicio;
import com.tecnicoya.api.model.enums.TipoUsuario;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Repository
public class EstadisticasDaoImpl implements IEstadisticasDao {
    @PersistenceContext
    private EntityManager em;

    @Override
    public Map<EstadoServicio, Long> contarServiciosPorEstado() {
        List<Object[]> filas = em.createQuery(
                        "SELECT s.estadoServicio, COUNT(s) FROM Servicio s GROUP BY s.estadoServicio", Object[].class)
                .getResultList();
        return completarConCeros(EstadoServicio.class, filas);
    }

    @Override
    public Map<TipoUsuario, Long> contarUsuariosPorTipo() {
        List<Object[]> filas = em.createQuery(
                        "SELECT u.tipoUsuario, COUNT(u) FROM Usuario u GROUP BY u.tipoUsuario", Object[].class)
                .getResultList();
        return completarConCeros(TipoUsuario.class, filas);
    }

    @Override
    public BigDecimal sumarPagosAprobados() {
        BigDecimal total = em.createQuery(
                        "SELECT SUM(p.montoPago) FROM PagoSuscripcion p WHERE p.estadoPago = :estado", BigDecimal.class)
                .setParameter("estado", EstadoPago.APROBADO)
                .getSingleResult();
        return Objects.requireNonNullElse(total, BigDecimal.ZERO);
    }

    @Override
    public double promedioCalificaciones() {
        Double promedio = em.createQuery("SELECT AVG(c.puntuacion) FROM Calificacion c", Double.class)
                .getSingleResult();
        return Objects.requireNonNullElse(promedio, 0.0);
    }

    private static <E extends Enum<E>> Map<E, Long> completarConCeros(Class<E> tipo, List<Object[]> filas) {
        EnumMap<E, Long> conteo = new EnumMap<>(tipo);
        for (E valor : tipo.getEnumConstants()) {
            conteo.put(valor, 0L);
        }
        for (Object[] fila : filas) {
            conteo.put(tipo.cast(fila[0]), (Long) fila[1]);
        }
        return Maps.immutableEnumMap(conteo);
    }
}
