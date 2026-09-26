package com.tecnicoya.api.dto.request;

import com.tecnicoya.api.entity.enums.EstadoValidacion;
import com.tecnicoya.api.entity.enums.MetodoPago;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Si no se envía {@code fechaPago} se registra la fecha y hora actual. */
public record EvidenciaPagoRequest(

        @NotNull(message = "El id del servicio es obligatorio")
        Long servicioId,

        @NotNull(message = "El monto es obligatorio")
        @DecimalMin(value = "0.01", message = "El monto debe ser mayor que 0")
        @Digits(integer = 8, fraction = 2, message = "El monto admite hasta 8 enteros y 2 decimales")
        BigDecimal monto,

        @NotNull(message = "El método de pago es obligatorio")
        MetodoPago metodoPago,

        @NotBlank(message = "El archivo de evidencia es obligatorio")
        @Size(max = 500, message = "La URL del archivo no debe superar los 500 caracteres")
        @Pattern(regexp = "^https?://\\S+$", message = "El archivo de evidencia debe ser una URL http:// o https://")
        String archivoEvidencia,

        @PastOrPresent(message = "La fecha de pago no puede ser futura")
        LocalDateTime fechaPago,

        EstadoValidacion estadoValidacion
) {
}
