package com.tecnicoya.api.dto.response;

import com.tecnicoya.api.entity.PagoSuscripcion;
import com.tecnicoya.api.entity.enums.EstadoPago;
import com.tecnicoya.api.entity.enums.MetodoPago;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PagoSuscripcionResponse(
        Long idPagoSuscripcion,
        Long suscripcionId,
        Long tecnicoId,
        String nombrePlan,
        BigDecimal montoPago,
        MetodoPago metodoPago,
        EstadoPago estadoPago,
        LocalDateTime fechaPago
) {
    public static PagoSuscripcionResponse desde(PagoSuscripcion pago) {
        return new PagoSuscripcionResponse(
                pago.getIdPagoSuscripcion(),
                pago.getSuscripcion().getIdSuscripcion(),
                pago.getSuscripcion().getTecnico().getIdTecnico(),
                pago.getSuscripcion().getPlan().getNombrePlan(),
                pago.getMontoPago(),
                pago.getMetodoPago(),
                pago.getEstadoPago(),
                pago.getFechaPago());
    }
}
