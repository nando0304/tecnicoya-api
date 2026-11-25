package com.tecnicoya.api.service.impl;

import com.tecnicoya.api.dao.IPagoSuscripcionDao;
import com.tecnicoya.api.dao.ISuscripcionDao;
import com.tecnicoya.api.dto.request.PagoSuscripcionRequest;
import com.tecnicoya.api.dto.response.PagoSuscripcionResponse;
import com.tecnicoya.api.exception.RecursoNoEncontradoException;
import com.tecnicoya.api.exception.ReglaNegocioException;
import com.tecnicoya.api.model.enums.EstadoPago;
import com.tecnicoya.api.model.enums.EstadoSuscripcion;
import com.tecnicoya.api.model.PagoSuscripcion;
import com.tecnicoya.api.model.Suscripcion;
import com.tecnicoya.api.security.ControlAcceso;
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
    private final IPagoSuscripcionDao pagoSuscripcionDao;
    private final ISuscripcionDao suscripcionDao;
    private final ControlAcceso controlAcceso;

    @Override
    public List<PagoSuscripcionResponse> listar() {
        return pagoSuscripcionDao.listar().stream().map(PagoSuscripcionResponse::desde).toList();
    }

    @Override
    public PagoSuscripcionResponse obtenerPorId(Long id) {
        return PagoSuscripcionResponse.desde(buscar(id));
    }

    @Override
    @Transactional
    public PagoSuscripcionResponse crear(PagoSuscripcionRequest request) {
        Suscripcion suscripcion = buscarSuscripcionNoCancelada(request.suscripcionId());
        controlAcceso.exigirPropietario("registrar un pago de la suscripción de otro técnico",
                suscripcion.getTecnico().getUsuario().getIdUsuario());
        if (request.estadoPago() != null && request.estadoPago() != EstadoPago.PENDIENTE) {
            controlAcceso.exigirAdministrador("registrar un pago en estado " + request.estadoPago());
        }
        PagoSuscripcion pago = new PagoSuscripcion();
        pago.setSuscripcion(suscripcion);
        pago.setEstadoPago(request.estadoPago() != null ? request.estadoPago() : EstadoPago.PENDIENTE);
        pago.setFechaPago(request.fechaPago() != null ? request.fechaPago() : FechaUtil.ahora());
        pago.setMontoPago(request.montoPago());
        pago.setMetodoPago(request.metodoPago());
        return PagoSuscripcionResponse.desde(pagoSuscripcionDao.guardar(pago));
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
        return PagoSuscripcionResponse.desde(pagoSuscripcionDao.guardar(pago));
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        pagoSuscripcionDao.eliminar(buscar(id));
    }

    private PagoSuscripcion buscar(Long id) {
        return pagoSuscripcionDao.buscarPorId(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("el pago de suscripción", id));
    }

    private Suscripcion buscarSuscripcionNoCancelada(Long suscripcionId) {
        Suscripcion suscripcion = suscripcionDao.buscarPorId(suscripcionId)
                .orElseThrow(() -> new RecursoNoEncontradoException("la suscripción", suscripcionId));
        if (suscripcion.getEstadoSuscripcion() == EstadoSuscripcion.CANCELADA) {
            throw new ReglaNegocioException("No se pueden registrar pagos para una suscripción CANCELADA");
        }
        return suscripcion;
    }
}
