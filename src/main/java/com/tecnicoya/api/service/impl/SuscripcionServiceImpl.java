package com.tecnicoya.api.service.impl;

import com.tecnicoya.api.dto.request.SuscripcionRequest;
import com.tecnicoya.api.dto.response.SuscripcionResponse;
import com.tecnicoya.api.entity.PlanSuscripcion;
import com.tecnicoya.api.entity.Suscripcion;
import com.tecnicoya.api.entity.Tecnico;
import com.tecnicoya.api.entity.enums.EstadoRegistro;
import com.tecnicoya.api.entity.enums.EstadoSuscripcion;
import com.tecnicoya.api.exception.ConflictoException;
import com.tecnicoya.api.exception.RecursoNoEncontradoException;
import com.tecnicoya.api.exception.ReglaNegocioException;
import com.tecnicoya.api.repository.PlanSuscripcionRepository;
import com.tecnicoya.api.repository.SuscripcionRepository;
import com.tecnicoya.api.repository.TecnicoRepository;
import com.tecnicoya.api.service.SuscripcionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/** Implementación de {@link SuscripcionService}. */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SuscripcionServiceImpl implements SuscripcionService {

    private final SuscripcionRepository suscripcionRepository;
    private final TecnicoRepository tecnicoRepository;
    private final PlanSuscripcionRepository planSuscripcionRepository;

    @Override
    public List<SuscripcionResponse> listar() {
        return suscripcionRepository.findAll().stream().map(SuscripcionResponse::desde).toList();
    }

    @Override
    public SuscripcionResponse obtenerPorId(Long id) {
        return SuscripcionResponse.desde(buscar(id));
    }

    @Override
    @Transactional
    public SuscripcionResponse crear(SuscripcionRequest request) {
        Suscripcion suscripcion = new Suscripcion();
        suscripcion.setTecnico(buscarTecnico(request.tecnicoId()));
        suscripcion.setPlan(buscarPlanActivo(request.planId()));
        suscripcion.setEstadoSuscripcion(request.estadoSuscripcion() != null
                ? request.estadoSuscripcion() : EstadoSuscripcion.PENDIENTE);
        aplicarFechasYValidar(request, suscripcion);
        return SuscripcionResponse.desde(suscripcionRepository.save(suscripcion));
    }

    @Override
    @Transactional
    public SuscripcionResponse actualizar(Long id, SuscripcionRequest request) {
        Suscripcion suscripcion = buscar(id);
        if (!suscripcion.getTecnico().getIdTecnico().equals(request.tecnicoId())) {
            suscripcion.setTecnico(buscarTecnico(request.tecnicoId()));
        }
        if (!suscripcion.getPlan().getIdPlan().equals(request.planId())) {
            suscripcion.setPlan(buscarPlanActivo(request.planId()));
        }
        if (request.estadoSuscripcion() != null) {
            suscripcion.setEstadoSuscripcion(request.estadoSuscripcion());
        }
        aplicarFechasYValidar(request, suscripcion);
        return SuscripcionResponse.desde(suscripcionRepository.save(suscripcion));
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        suscripcionRepository.delete(buscar(id));
        suscripcionRepository.flush();
    }

    private Suscripcion buscar(Long id) {
        return suscripcionRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("la suscripción", id));
    }

    private void aplicarFechasYValidar(SuscripcionRequest request, Suscripcion suscripcion) {
        if (!request.fechaFin().isAfter(request.fechaInicio())) {
            throw new ReglaNegocioException("La fecha de fin debe ser posterior a la fecha de inicio");
        }
        suscripcion.setFechaInicio(request.fechaInicio());
        suscripcion.setFechaFin(request.fechaFin());

        EstadoSuscripcion estado = suscripcion.getEstadoSuscripcion();
        if (estado == EstadoSuscripcion.CANCELADA) {
            LocalDate cancelacion = request.fechaCancelacion() != null ? request.fechaCancelacion()
                    : suscripcion.getFechaCancelacion() != null ? suscripcion.getFechaCancelacion()
                    : LocalDate.now();
            suscripcion.setFechaCancelacion(cancelacion);
        } else {
            if (request.fechaCancelacion() != null) {
                throw new ReglaNegocioException("La fecha de cancelación solo aplica a suscripciones en estado CANCELADA");
            }
            suscripcion.setFechaCancelacion(null);
        }

        if (estado == EstadoSuscripcion.ACTIVA && tieneOtraSuscripcionActiva(suscripcion)) {
            throw new ConflictoException("El técnico ya tiene una suscripción ACTIVA; "
                    + "cancele o finalice la actual antes de activar otra");
        }
    }

    private boolean tieneOtraSuscripcionActiva(Suscripcion suscripcion) {
        Long tecnicoId = suscripcion.getTecnico().getIdTecnico();
        return suscripcion.getIdSuscripcion() == null
                ? suscripcionRepository.existsByTecnico_IdTecnicoAndEstadoSuscripcion(tecnicoId, EstadoSuscripcion.ACTIVA)
                : suscripcionRepository.existsByTecnico_IdTecnicoAndEstadoSuscripcionAndIdSuscripcionNot(
                        tecnicoId, EstadoSuscripcion.ACTIVA, suscripcion.getIdSuscripcion());
    }

    private Tecnico buscarTecnico(Long tecnicoId) {
        return tecnicoRepository.findById(tecnicoId)
                .orElseThrow(() -> new RecursoNoEncontradoException("el técnico", tecnicoId));
    }

    private PlanSuscripcion buscarPlanActivo(Long planId) {
        PlanSuscripcion plan = planSuscripcionRepository.findById(planId)
                .orElseThrow(() -> new RecursoNoEncontradoException("el plan de suscripción", planId));
        if (plan.getEstado() != EstadoRegistro.ACTIVO) {
            throw new ReglaNegocioException("El plan '" + plan.getNombrePlan() + "' no está ACTIVO");
        }
        return plan;
    }
}
