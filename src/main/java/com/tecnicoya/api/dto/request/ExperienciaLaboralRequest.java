package com.tecnicoya.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/** Si {@code actualidad} es true no debe enviarse {@code fechaFin}; si es false, es obligatoria. */
public record ExperienciaLaboralRequest(

        @NotNull(message = "El id del técnico es obligatorio")
        Long tecnicoId,

        @NotBlank(message = "La empresa es obligatoria")
        @Size(max = 150, message = "La empresa no debe superar los 150 caracteres")
        String empresa,

        @NotBlank(message = "El cargo es obligatorio")
        @Size(max = 100, message = "El cargo no debe superar los 100 caracteres")
        String cargo,

        @Size(max = 1000, message = "La descripción no debe superar los 1000 caracteres")
        String descripcion,

        @NotNull(message = "La fecha de inicio es obligatoria")
        @PastOrPresent(message = "La fecha de inicio no puede ser futura")
        LocalDate fechaInicio,

        @PastOrPresent(message = "La fecha de fin no puede ser futura")
        LocalDate fechaFin,

        @NotNull(message = "Debe indicar si es el trabajo actual (actualidad)")
        Boolean actualidad
) {
}
