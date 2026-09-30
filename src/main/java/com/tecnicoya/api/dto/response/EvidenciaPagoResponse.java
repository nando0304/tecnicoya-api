package com.tecnicoya.api.dto.response;

import com.tecnicoya.api.entity.EvidenciaPago;
import com.tecnicoya.api.entity.enums.EstadoValidacion;
import com.tecnicoya.api.entity.enums.MetodoPago;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Evidencia de pago devuelta por la API, con el título del servicio. */
public record EvidenciaPagoResponse(
        Long idEvidenciaPago,
        Long servicioId,
        String servicioTitulo,
        BigDecimal monto,
        MetodoPago metodoPago,
        String archivoEvidencia,
        LocalDateTime fechaPago,
        EstadoValidacion estadoValidacion
) {
    public static EvidenciaPagoResponse desde(EvidenciaPago evidenciaPago) {
        return new EvidenciaPagoResponse(
                evidenciaPago.getIdEvidenciaPago(),
                evidenciaPago.getServicio().getIdServicio(),
                evidenciaPago.getServicio().getTitulo(),
                evidenciaPago.getMonto(),
                evidenciaPago.getMetodoPago(),
                evidenciaPago.getArchivoEvidencia(),
                evidenciaPago.getFechaPago(),
                evidenciaPago.getEstadoValidacion());
    }
}
