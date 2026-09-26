package com.tecnicoya.api.service.impl;

import com.tecnicoya.api.dto.request.EvidenciaPagoRequest;
import com.tecnicoya.api.dto.response.EvidenciaPagoResponse;
import com.tecnicoya.api.entity.EvidenciaPago;
import com.tecnicoya.api.entity.Servicio;
import com.tecnicoya.api.entity.enums.EstadoServicio;
import com.tecnicoya.api.entity.enums.EstadoValidacion;
import com.tecnicoya.api.exception.RecursoNoEncontradoException;
import com.tecnicoya.api.exception.ReglaNegocioException;
import com.tecnicoya.api.repository.EvidenciaPagoRepository;
import com.tecnicoya.api.repository.ServicioRepository;
import com.tecnicoya.api.service.EvidenciaPagoService;
import com.tecnicoya.api.util.FechaUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EvidenciaPagoServiceImpl implements EvidenciaPagoService {

    private final EvidenciaPagoRepository evidenciaPagoRepository;
    private final ServicioRepository servicioRepository;

    @Override
    public List<EvidenciaPagoResponse> listar() {
        return evidenciaPagoRepository.findAll().stream().map(EvidenciaPagoResponse::desde).toList();
    }

    @Override
    public EvidenciaPagoResponse obtenerPorId(Long id) {
        return EvidenciaPagoResponse.desde(buscar(id));
    }

    @Override
    @Transactional
    public EvidenciaPagoResponse crear(EvidenciaPagoRequest request) {
        EvidenciaPago evidenciaPago = new EvidenciaPago();
        evidenciaPago.setServicio(buscarServicioPagable(request.servicioId()));
        evidenciaPago.setFechaPago(request.fechaPago() != null ? request.fechaPago() : FechaUtil.ahora());
        evidenciaPago.setEstadoValidacion(request.estadoValidacion() != null
                ? request.estadoValidacion() : EstadoValidacion.PENDIENTE);
        copiarDatos(request, evidenciaPago);
        return EvidenciaPagoResponse.desde(evidenciaPagoRepository.save(evidenciaPago));
    }

    @Override
    @Transactional
    public EvidenciaPagoResponse actualizar(Long id, EvidenciaPagoRequest request) {
        EvidenciaPago evidenciaPago = buscar(id);
        if (!evidenciaPago.getServicio().getIdServicio().equals(request.servicioId())) {
            evidenciaPago.setServicio(buscarServicioPagable(request.servicioId()));
        }
        if (request.fechaPago() != null) {
            evidenciaPago.setFechaPago(request.fechaPago());
        }
        if (request.estadoValidacion() != null) {
            evidenciaPago.setEstadoValidacion(request.estadoValidacion());
        }
        copiarDatos(request, evidenciaPago);
        return EvidenciaPagoResponse.desde(evidenciaPagoRepository.save(evidenciaPago));
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        evidenciaPagoRepository.delete(buscar(id));
        evidenciaPagoRepository.flush();
    }

    private EvidenciaPago buscar(Long id) {
        return evidenciaPagoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("la evidencia de pago", id));
    }

    private Servicio buscarServicioPagable(Long servicioId) {
        Servicio servicio = servicioRepository.findById(servicioId)
                .orElseThrow(() -> new RecursoNoEncontradoException("el servicio", servicioId));
        if (servicio.getEstadoServicio() == EstadoServicio.CANCELADO) {
            throw new ReglaNegocioException("No se puede registrar un pago para un servicio CANCELADO");
        }
        if (servicio.getTecnico() == null) {
            throw new ReglaNegocioException("El servicio con id " + servicioId
                    + " aún no tiene un técnico asignado; no se puede registrar un pago");
        }
        return servicio;
    }

    private static void copiarDatos(EvidenciaPagoRequest request, EvidenciaPago evidenciaPago) {
        evidenciaPago.setMonto(request.monto());
        evidenciaPago.setMetodoPago(request.metodoPago());
        evidenciaPago.setArchivoEvidencia(request.archivoEvidencia().trim());
    }
}
