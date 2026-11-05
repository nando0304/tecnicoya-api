package com.tecnicoya.api.service.impl;

import com.tecnicoya.api.dao.ICalificacionDao;
import com.tecnicoya.api.dao.IServicioDao;
import com.tecnicoya.api.dto.request.CalificacionRequest;
import com.tecnicoya.api.dto.response.CalificacionResponse;
import com.tecnicoya.api.exception.ConflictoException;
import com.tecnicoya.api.exception.RecursoNoEncontradoException;
import com.tecnicoya.api.exception.ReglaNegocioException;
import com.tecnicoya.api.model.Calificacion;
import com.tecnicoya.api.model.enums.EstadoServicio;
import com.tecnicoya.api.model.Servicio;
import com.tecnicoya.api.service.CalificacionService;
import com.tecnicoya.api.util.FechaUtil;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** Implementación de {@link CalificacionService}. */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CalificacionServiceImpl implements CalificacionService {

    private final ICalificacionDao calificacionDao;
    private final IServicioDao servicioDao;

    @Override
    public List<CalificacionResponse> listar() {
        return calificacionDao.listar().stream().map(CalificacionResponse::desde).toList();
    }

    @Override
    public CalificacionResponse obtenerPorId(Long id) {
        return CalificacionResponse.desde(buscar(id));
    }

    @Override
    @Transactional
    public CalificacionResponse crear(CalificacionRequest request) {
        Servicio servicio = buscarServicioFinalizado(request.servicioId());
        if (calificacionDao.existePorServicio(servicio.getIdServicio())) {
            throw new ConflictoException("El servicio con id " + servicio.getIdServicio() + " ya fue calificado");
        }

        Calificacion calificacion = new Calificacion();
        calificacion.setServicio(servicio);
        calificacion.setPuntuacion(request.puntuacion());
        calificacion.setComentario(StringUtils.stripToNull(request.comentario()));
        calificacion.setFechaCalificacion(FechaUtil.ahora());
        return CalificacionResponse.desde(calificacionDao.guardar(calificacion));
    }

    @Override
    @Transactional
    public CalificacionResponse actualizar(Long id, CalificacionRequest request) {
        Calificacion calificacion = buscar(id);
        if (!calificacion.getServicio().getIdServicio().equals(request.servicioId())) {
            Servicio servicio = buscarServicioFinalizado(request.servicioId());
            if (calificacionDao.existePorServicioEnOtraCalificacion(servicio.getIdServicio(), id)) {
                throw new ConflictoException("El servicio con id " + servicio.getIdServicio() + " ya fue calificado");
            }
            calificacion.setServicio(servicio);
        }
        calificacion.setPuntuacion(request.puntuacion());
        calificacion.setComentario(StringUtils.stripToNull(request.comentario()));
        return CalificacionResponse.desde(calificacionDao.guardar(calificacion));
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        calificacionDao.eliminar(buscar(id));
    }

    private Calificacion buscar(Long id) {
        return calificacionDao.buscarPorId(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("la calificación", id));
    }

    private Servicio buscarServicioFinalizado(Long servicioId) {
        Servicio servicio = servicioDao.buscarPorId(servicioId)
                .orElseThrow(() -> new RecursoNoEncontradoException("el servicio", servicioId));
        if (servicio.getEstadoServicio() != EstadoServicio.FINALIZADO) {
            throw new ReglaNegocioException("Solo se pueden calificar servicios FINALIZADOS; el servicio con id "
                    + servicioId + " está en estado " + servicio.getEstadoServicio());
        }
        return servicio;
    }
}
