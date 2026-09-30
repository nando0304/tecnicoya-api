package com.tecnicoya.api.dto.request;

import com.tecnicoya.api.entity.enums.EstadoRegistro;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/** Datos para registrar o actualizar un plan de suscripción. */
public record PlanSuscripcionRequest(

        @NotBlank(message = "El nombre del plan es obligatorio")
        @Size(max = 100, message = "El nombre del plan no debe superar los 100 caracteres")
        String nombrePlan,

        @NotNull(message = "El precio inicial es obligatorio")
        @DecimalMin(value = "0.00", message = "El precio inicial no puede ser negativo")
        @Digits(integer = 8, fraction = 2, message = "El precio inicial admite hasta 8 enteros y 2 decimales")
        BigDecimal precioInicial,

        @NotNull(message = "El límite de clientes es obligatorio")
        @Min(value = 1, message = "El límite de clientes debe ser al menos 1")
        Integer limiteClientes,

        @NotNull(message = "El precio posterior es obligatorio")
        @DecimalMin(value = "0.00", message = "El precio posterior no puede ser negativo")
        @Digits(integer = 8, fraction = 2, message = "El precio posterior admite hasta 8 enteros y 2 decimales")
        BigDecimal precioPosterior,

        @Size(max = 500, message = "La descripción no debe superar los 500 caracteres")
        String descripcion,

        EstadoRegistro estado
) {
}
