package com.tecnicoya.api.dto.request;

import com.tecnicoya.api.entity.enums.EstadoPago;
import com.tecnicoya.api.entity.enums.MetodoPago;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Si no se envía {@code fechaPago} se registra la fecha y hora actual. */
public record PagoSuscripcionRequest(

        @NotNull(message = "El id de la suscripción es obligatorio")
        Long suscripcionId,

        @NotNull(message = "El monto del pago es obligatorio")
        @DecimalMin(value = "0.01", message = "El monto del pago debe ser mayor que 0")
        @Digits(integer = 8, fraction = 2, message = "El monto del pago admite hasta 8 enteros y 2 decimales")
        BigDecimal montoPago,

        @NotNull(message = "El método de pago es obligatorio")
        MetodoPago metodoPago,

        EstadoPago estadoPago,

        @PastOrPresent(message = "La fecha de pago no puede ser futura")
        LocalDateTime fechaPago
) {
}
