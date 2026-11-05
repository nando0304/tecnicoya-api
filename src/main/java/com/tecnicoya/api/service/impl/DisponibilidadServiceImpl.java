package com.tecnicoya.api.service.impl;

import com.tecnicoya.api.dao.IDisponibilidadDao;
import com.tecnicoya.api.dao.ITecnicoDao;
import com.tecnicoya.api.dto.request.DisponibilidadRequest;
import com.tecnicoya.api.dto.response.DisponibilidadResponse;
import com.tecnicoya.api.exception.ConflictoException;
import com.tecnicoya.api.exception.RecursoNoEncontradoException;
import com.tecnicoya.api.exception.ReglaNegocioException;
import com.tecnicoya.api.model.Disponibilidad;
import com.tecnicoya.api.model.enums.EstadoRegistro;
import com.tecnicoya.api.model.Tecnico;
import com.tecnicoya.api.service.DisponibilidadService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** Implementación de {@link DisponibilidadService}. */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DisponibilidadServiceImpl implements DisponibilidadService {

    private final IDisponibilidadDao disponibilidadDao;
    private final ITecnicoDao tecnicoDao;

    @Override
    public List<DisponibilidadResponse> listar() {
        return disponibilidadDao.listar().stream().map(DisponibilidadResponse::desde).toList();
    }

    @Override
    public DisponibilidadResponse obtenerPorId(Long id) {
        return DisponibilidadResponse.desde(buscar(id));
    }

    @Override
    @Transactional
    public DisponibilidadResponse crear(DisponibilidadRequest request) {
        Disponibilidad disponibilidad = new Disponibilidad();
        disponibilidad.setEstado(request.estado() != null ? request.estado() : EstadoRegistro.ACTIVO);
        aplicarDatos(request, disponibilidad);
        return DisponibilidadResponse.desde(disponibilidadDao.guardar(disponibilidad));
    }

    @Override
    @Transactional
    public DisponibilidadResponse actualizar(Long id, DisponibilidadRequest request) {
        Disponibilidad disponibilidad = buscar(id);
        if (request.estado() != null) {
            disponibilidad.setEstado(request.estado());
        }
        aplicarDatos(request, disponibilidad);
        return DisponibilidadResponse.desde(disponibilidadDao.guardar(disponibilidad));
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        disponibilidadDao.eliminar(buscar(id));
    }

    private Disponibilidad buscar(Long id) {
        return disponibilidadDao.buscarPorId(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("la disponibilidad", id));
    }

    private void aplicarDatos(DisponibilidadRequest request, Disponibilidad disponibilidad) {
        if (!request.horaFin().isAfter(request.horaInicio())) {
            throw new ReglaNegocioException("La hora de fin debe ser posterior a la hora de inicio");
        }
        Tecnico tecnico = tecnicoDao.buscarPorId(request.tecnicoId())
                .orElseThrow(() -> new RecursoNoEncontradoException("el técnico", request.tecnicoId()));

        long cruces = disponibilidadDao.contarCrucesDeHorario(tecnico.getIdTecnico(), request.diaSemana(),
                request.horaInicio(), request.horaFin(), disponibilidad.getIdDisponibilidad());
        if (cruces > 0) {
            throw new ConflictoException(String.format(
                    "El técnico ya tiene una franja el %s que se cruza con el horario %s - %s",
                    request.diaSemana(), request.horaInicio(), request.horaFin()));
        }

        disponibilidad.setTecnico(tecnico);
        disponibilidad.setDiaSemana(request.diaSemana());
        disponibilidad.setHoraInicio(request.horaInicio());
        disponibilidad.setHoraFin(request.horaFin());
    }
}
