package com.tecnicoya.api.service.impl;

import com.tecnicoya.api.dto.request.DisponibilidadRequest;
import com.tecnicoya.api.dto.response.DisponibilidadResponse;
import com.tecnicoya.api.entity.Disponibilidad;
import com.tecnicoya.api.entity.Tecnico;
import com.tecnicoya.api.entity.enums.EstadoRegistro;
import com.tecnicoya.api.exception.ConflictoException;
import com.tecnicoya.api.exception.RecursoNoEncontradoException;
import com.tecnicoya.api.exception.ReglaNegocioException;
import com.tecnicoya.api.repository.DisponibilidadRepository;
import com.tecnicoya.api.repository.TecnicoRepository;
import com.tecnicoya.api.service.DisponibilidadService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DisponibilidadServiceImpl implements DisponibilidadService {

    private final DisponibilidadRepository disponibilidadRepository;
    private final TecnicoRepository tecnicoRepository;

    @Override
    public List<DisponibilidadResponse> listar() {
        return disponibilidadRepository.findAll().stream().map(DisponibilidadResponse::desde).toList();
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
        return DisponibilidadResponse.desde(disponibilidadRepository.save(disponibilidad));
    }

    @Override
    @Transactional
    public DisponibilidadResponse actualizar(Long id, DisponibilidadRequest request) {
        Disponibilidad disponibilidad = buscar(id);
        if (request.estado() != null) {
            disponibilidad.setEstado(request.estado());
        }
        aplicarDatos(request, disponibilidad);
        return DisponibilidadResponse.desde(disponibilidadRepository.save(disponibilidad));
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        disponibilidadRepository.delete(buscar(id));
        disponibilidadRepository.flush();
    }

    private Disponibilidad buscar(Long id) {
        return disponibilidadRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("la disponibilidad", id));
    }

    private void aplicarDatos(DisponibilidadRequest request, Disponibilidad disponibilidad) {
        if (!request.horaFin().isAfter(request.horaInicio())) {
            throw new ReglaNegocioException("La hora de fin debe ser posterior a la hora de inicio");
        }
        Tecnico tecnico = tecnicoRepository.findById(request.tecnicoId())
                .orElseThrow(() -> new RecursoNoEncontradoException("el técnico", request.tecnicoId()));

        long cruces = disponibilidadRepository.contarCrucesDeHorario(tecnico.getIdTecnico(), request.diaSemana(),
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
