package com.tecnicoya.api.service.impl;

import com.tecnicoya.api.dto.request.PlanSuscripcionRequest;
import com.tecnicoya.api.dto.response.PlanSuscripcionResponse;
import com.tecnicoya.api.entity.PlanSuscripcion;
import com.tecnicoya.api.entity.enums.EstadoRegistro;
import com.tecnicoya.api.exception.ConflictoException;
import com.tecnicoya.api.exception.RecursoNoEncontradoException;
import com.tecnicoya.api.repository.PlanSuscripcionRepository;
import com.tecnicoya.api.service.PlanSuscripcionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** Implementación de {@link PlanSuscripcionService}. */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PlanSuscripcionServiceImpl implements PlanSuscripcionService {

    private final PlanSuscripcionRepository planSuscripcionRepository;

    @Override
    public List<PlanSuscripcionResponse> listar() {
        return planSuscripcionRepository.findAll().stream().map(PlanSuscripcionResponse::desde).toList();
    }

    @Override
    public PlanSuscripcionResponse obtenerPorId(Long id) {
        return PlanSuscripcionResponse.desde(buscar(id));
    }

    @Override
    @Transactional
    public PlanSuscripcionResponse crear(PlanSuscripcionRequest request) {
        String nombre = request.nombrePlan().trim();
        if (planSuscripcionRepository.existsByNombrePlanIgnoreCase(nombre)) {
            throw new ConflictoException("Ya existe un plan con el nombre '" + nombre + "'");
        }

        PlanSuscripcion plan = new PlanSuscripcion();
        copiarDatos(request, plan, nombre);
        plan.setEstado(request.estado() != null ? request.estado() : EstadoRegistro.ACTIVO);
        return PlanSuscripcionResponse.desde(planSuscripcionRepository.save(plan));
    }

    @Override
    @Transactional
    public PlanSuscripcionResponse actualizar(Long id, PlanSuscripcionRequest request) {
        PlanSuscripcion plan = buscar(id);
        String nombre = request.nombrePlan().trim();
        if (planSuscripcionRepository.existsByNombrePlanIgnoreCaseAndIdPlanNot(nombre, id)) {
            throw new ConflictoException("Ya existe otro plan con el nombre '" + nombre + "'");
        }

        copiarDatos(request, plan, nombre);
        if (request.estado() != null) {
            plan.setEstado(request.estado());
        }
        return PlanSuscripcionResponse.desde(planSuscripcionRepository.save(plan));
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        planSuscripcionRepository.delete(buscar(id));
        planSuscripcionRepository.flush();
    }

    private PlanSuscripcion buscar(Long id) {
        return planSuscripcionRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("el plan de suscripción", id));
    }

    private static void copiarDatos(PlanSuscripcionRequest request, PlanSuscripcion plan, String nombre) {
        plan.setNombrePlan(nombre);
        plan.setPrecioInicial(request.precioInicial());
        plan.setLimiteClientes(request.limiteClientes());
        plan.setPrecioPosterior(request.precioPosterior());
        plan.setDescripcion(request.descripcion());
    }
}
