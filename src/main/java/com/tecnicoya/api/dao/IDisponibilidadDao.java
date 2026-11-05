package com.tecnicoya.api.dao;

import com.tecnicoya.api.model.Disponibilidad;
import com.tecnicoya.api.model.enums.DiaSemana;

import java.time.LocalTime;

/** DAO de {@link Disponibilidad} (franjas horarias de atención de un técnico). */
public interface IDisponibilidadDao extends IGenericDao<Disponibilidad, Long> {

    /**
     * Cuenta las franjas del técnico que se cruzan con {@code [horaInicio, horaFin)} el mismo día.
     *
     * @param tecnicoId  técnico dueño de las franjas
     * @param diaSemana  día a revisar
     * @param horaInicio inicio de la franja nueva
     * @param horaFin    fin de la franja nueva
     * @param excluirId  franja que se está actualizando, para no compararla consigo misma;
     *                   {@code null} al crear
     * @return cantidad de franjas que se cruzan
     */
    long contarCrucesDeHorario(Long tecnicoId, DiaSemana diaSemana, LocalTime horaInicio, LocalTime horaFin,
                               Long excluirId);
}
