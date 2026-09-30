package com.tecnicoya.api.service.impl;

import com.tecnicoya.api.dto.request.CalificacionRequest;
import com.tecnicoya.api.dto.response.CalificacionResponse;
import com.tecnicoya.api.entity.Calificacion;
import com.tecnicoya.api.entity.Servicio;
import com.tecnicoya.api.entity.enums.EstadoServicio;
import com.tecnicoya.api.exception.ConflictoException;
import com.tecnicoya.api.exception.RecursoNoEncontradoException;
import com.tecnicoya.api.exception.ReglaNegocioException;
import com.tecnicoya.api.repository.CalificacionRepository;
import com.tecnicoya.api.repository.ServicioRepository;
import com.tecnicoya.api.service.CalificacionService;
import com.tecnicoya.api.util.FechaUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** Implementación de {@link CalificacionService}. */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CalificacionServiceImpl implements CalificacionService {

    private final CalificacionRepository calificacionRepository;
    private final ServicioRepository servicioRepository;

    @Override
    public List<CalificacionResponse> listar() {
        return calificacionRepository.findAll().stream().map(CalificacionResponse::desde).toList();
    }

    @Override
    public CalificacionResponse obtenerPorId(Long id) {
        return CalificacionResponse.desde(buscar(id));
    }

    @Override
    @Transactional
    public CalificacionResponse crear(CalificacionRequest request) {
        Servicio servicio = buscarServicioFinalizado(request.servicioId());
        if (calificacionRepository.existsByServicio_IdServicio(servicio.getIdServicio())) {
            throw new ConflictoException("El servicio con id " + servicio.getIdServicio() + " ya fue calificado");
        }

        Calificacion calificacion = new Calificacion();
        calificacion.setServicio(servicio);
        calificacion.setPuntuacion(request.puntuacion());
        calificacion.setComentario(request.comentario());
        calificacion.setFechaCalificacion(FechaUtil.ahora());
        return CalificacionResponse.desde(calificacionRepository.save(calificacion));
    }

    @Override
    @Transactional
    public CalificacionResponse actualizar(Long id, CalificacionRequest request) {
        Calificacion calificacion = buscar(id);
        if (!calificacion.getServicio().getIdServicio().equals(request.servicioId())) {
            Servicio servicio = buscarServicioFinalizado(request.servicioId());
            if (calificacionRepository.existsByServicio_IdServicioAndIdCalificacionNot(servicio.getIdServicio(), id)) {
                throw new ConflictoException("El servicio con id " + servicio.getIdServicio() + " ya fue calificado");
            }
            calificacion.setServicio(servicio);
        }
        calificacion.setPuntuacion(request.puntuacion());
        calificacion.setComentario(request.comentario());
        return CalificacionResponse.desde(calificacionRepository.save(calificacion));
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        calificacionRepository.delete(buscar(id));
        calificacionRepository.flush();
    }

    private Calificacion buscar(Long id) {
        return calificacionRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("la calificación", id));
    }

    private Servicio buscarServicioFinalizado(Long servicioId) {
        Servicio servicio = servicioRepository.findById(servicioId)
                .orElseThrow(() -> new RecursoNoEncontradoException("el servicio", servicioId));
        if (servicio.getEstadoServicio() != EstadoServicio.FINALIZADO) {
            throw new ReglaNegocioException("Solo se pueden calificar servicios FINALIZADOS; el servicio con id "
                    + servicioId + " está en estado " + servicio.getEstadoServicio());
        }
        return servicio;
    }
}
