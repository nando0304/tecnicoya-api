package com.tecnicoya.api.service.impl;

import com.tecnicoya.api.dto.request.PagoSuscripcionRequest;
import com.tecnicoya.api.dto.response.PagoSuscripcionResponse;
import com.tecnicoya.api.entity.PagoSuscripcion;
import com.tecnicoya.api.entity.Suscripcion;
import com.tecnicoya.api.entity.enums.EstadoPago;
import com.tecnicoya.api.entity.enums.EstadoSuscripcion;
import com.tecnicoya.api.exception.RecursoNoEncontradoException;
import com.tecnicoya.api.exception.ReglaNegocioException;
import com.tecnicoya.api.repository.PagoSuscripcionRepository;
import com.tecnicoya.api.repository.SuscripcionRepository;
import com.tecnicoya.api.service.PagoSuscripcionService;
import com.tecnicoya.api.util.FechaUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PagoSuscripcionServiceImpl implements PagoSuscripcionService {

    private final PagoSuscripcionRepository pagoSuscripcionRepository;
    private final SuscripcionRepository suscripcionRepository;

    @Override
    public List<PagoSuscripcionResponse> listar() {
        return pagoSuscripcionRepository.findAll().stream().map(PagoSuscripcionResponse::desde).toList();
    }

    @Override
    public PagoSuscripcionResponse obtenerPorId(Long id) {
        return PagoSuscripcionResponse.desde(buscar(id));
    }

    @Override
    @Transactional
    public PagoSuscripcionResponse crear(PagoSuscripcionRequest request) {
        PagoSuscripcion pago = new PagoSuscripcion();
        pago.setSuscripcion(buscarSuscripcionNoCancelada(request.suscripcionId()));
        pago.setEstadoPago(request.estadoPago() != null ? request.estadoPago() : EstadoPago.PENDIENTE);
        pago.setFechaPago(request.fechaPago() != null ? request.fechaPago() : FechaUtil.ahora());
        pago.setMontoPago(request.montoPago());
        pago.setMetodoPago(request.metodoPago());
        return PagoSuscripcionResponse.desde(pagoSuscripcionRepository.save(pago));
    }

    @Override
    @Transactional
    public PagoSuscripcionResponse actualizar(Long id, PagoSuscripcionRequest request) {
        PagoSuscripcion pago = buscar(id);
        if (!pago.getSuscripcion().getIdSuscripcion().equals(request.suscripcionId())) {
            pago.setSuscripcion(buscarSuscripcionNoCancelada(request.suscripcionId()));
        }
        if (request.estadoPago() != null) {
            pago.setEstadoPago(request.estadoPago());
        }
        if (request.fechaPago() != null) {
            pago.setFechaPago(request.fechaPago());
        }
        pago.setMontoPago(request.montoPago());
        pago.setMetodoPago(request.metodoPago());
        return PagoSuscripcionResponse.desde(pagoSuscripcionRepository.save(pago));
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        pagoSuscripcionRepository.delete(buscar(id));
        pagoSuscripcionRepository.flush();
    }

    private PagoSuscripcion buscar(Long id) {
        return pagoSuscripcionRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("el pago de suscripción", id));
    }

    private Suscripcion buscarSuscripcionNoCancelada(Long suscripcionId) {
        Suscripcion suscripcion = suscripcionRepository.findById(suscripcionId)
                .orElseThrow(() -> new RecursoNoEncontradoException("la suscripción", suscripcionId));
        if (suscripcion.getEstadoSuscripcion() == EstadoSuscripcion.CANCELADA) {
            throw new ReglaNegocioException("No se pueden registrar pagos para una suscripción CANCELADA");
        }
        return suscripcion;
    }
}
