package com.tecnicoya.api.dto.response;

import com.tecnicoya.api.model.EvidenciaPago;
import com.tecnicoya.api.model.enums.EstadoValidacion;
import com.tecnicoya.api.model.enums.MetodoPago;

import java.math.BigDecimal;
import java.time.LocalDateTime;

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
