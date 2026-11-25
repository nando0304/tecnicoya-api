package com.tecnicoya.api.dao;

import com.tecnicoya.api.model.Disponibilidad;
import com.tecnicoya.api.model.enums.DiaSemana;

import java.time.LocalTime;

public interface IDisponibilidadDao extends IGenericDao<Disponibilidad, Long> {
    long contarCrucesDeHorario(Long tecnicoId, DiaSemana diaSemana, LocalTime horaInicio, LocalTime horaFin,
                               Long excluirId);
}
