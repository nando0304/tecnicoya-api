package com.tecnicoya.api.service.impl;

import com.tecnicoya.api.dao.IPlanSuscripcionDao;
import com.tecnicoya.api.dto.request.PlanSuscripcionRequest;
import com.tecnicoya.api.dto.response.PlanSuscripcionResponse;
import com.tecnicoya.api.exception.ConflictoException;
import com.tecnicoya.api.exception.RecursoNoEncontradoException;
import com.tecnicoya.api.model.enums.EstadoRegistro;
import com.tecnicoya.api.model.PlanSuscripcion;
import com.tecnicoya.api.service.PlanSuscripcionService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** Implementación de {@link PlanSuscripcionService}. */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PlanSuscripcionServiceImpl implements PlanSuscripcionService {

    private final IPlanSuscripcionDao planSuscripcionDao;

    @Override
    public List<PlanSuscripcionResponse> listar() {
        return planSuscripcionDao.listar().stream().map(PlanSuscripcionResponse::desde).toList();
    }

    @Override
    public PlanSuscripcionResponse obtenerPorId(Long id) {
        return PlanSuscripcionResponse.desde(buscar(id));
    }

    @Override
    @Transactional
    public PlanSuscripcionResponse crear(PlanSuscripcionRequest request) {
        String nombre = StringUtils.normalizeSpace(request.nombrePlan());
        if (planSuscripcionDao.existeNombre(nombre)) {
            throw new ConflictoException("Ya existe un plan con el nombre '" + nombre + "'");
        }

        PlanSuscripcion plan = new PlanSuscripcion();
        copiarDatos(request, plan, nombre);
        plan.setEstado(request.estado() != null ? request.estado() : EstadoRegistro.ACTIVO);
        return PlanSuscripcionResponse.desde(planSuscripcionDao.guardar(plan));
    }

    @Override
    @Transactional
    public PlanSuscripcionResponse actualizar(Long id, PlanSuscripcionRequest request) {
        PlanSuscripcion plan = buscar(id);
        String nombre = StringUtils.normalizeSpace(request.nombrePlan());
        if (planSuscripcionDao.existeNombreEnOtroPlan(nombre, id)) {
            throw new ConflictoException("Ya existe otro plan con el nombre '" + nombre + "'");
        }

        copiarDatos(request, plan, nombre);
        if (request.estado() != null) {
            plan.setEstado(request.estado());
        }
        return PlanSuscripcionResponse.desde(planSuscripcionDao.guardar(plan));
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        planSuscripcionDao.eliminar(buscar(id));
    }

    private PlanSuscripcion buscar(Long id) {
        return planSuscripcionDao.buscarPorId(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("el plan de suscripción", id));
    }

    private static void copiarDatos(PlanSuscripcionRequest request, PlanSuscripcion plan, String nombre) {
        plan.setNombrePlan(nombre);
        plan.setPrecioInicial(request.precioInicial());
        plan.setLimiteClientes(request.limiteClientes());
        plan.setPrecioPosterior(request.precioPosterior());
        plan.setDescripcion(StringUtils.stripToNull(request.descripcion()));
    }
}
