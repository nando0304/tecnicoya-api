package com.tecnicoya.api.service.impl;

import com.tecnicoya.api.dao.IEvidenciaPagoDao;
import com.tecnicoya.api.dao.IServicioDao;
import com.tecnicoya.api.dto.request.EvidenciaPagoRequest;
import com.tecnicoya.api.dto.response.EvidenciaPagoResponse;
import com.tecnicoya.api.exception.RecursoNoEncontradoException;
import com.tecnicoya.api.exception.ReglaNegocioException;
import com.tecnicoya.api.model.enums.EstadoServicio;
import com.tecnicoya.api.model.enums.EstadoValidacion;
import com.tecnicoya.api.model.EvidenciaPago;
import com.tecnicoya.api.model.Servicio;
import com.tecnicoya.api.security.ControlAcceso;
import com.tecnicoya.api.service.EvidenciaPagoService;
import com.tecnicoya.api.util.FechaUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Implementación de {@link EvidenciaPagoService}.
 *
 * <p>Control de acceso ({@link ControlAcceso}): solo el cliente o el técnico del servicio
 * registran su pago, y siempre queda PENDIENTE; el administrador lo valida, edita o elimina.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EvidenciaPagoServiceImpl implements EvidenciaPagoService {

    private final IEvidenciaPagoDao evidenciaPagoDao;
    private final IServicioDao servicioDao;
    private final ControlAcceso controlAcceso;

    @Override
    public List<EvidenciaPagoResponse> listar() {
        return evidenciaPagoDao.listar().stream().map(EvidenciaPagoResponse::desde).toList();
    }

    @Override
    public EvidenciaPagoResponse obtenerPorId(Long id) {
        return EvidenciaPagoResponse.desde(buscar(id));
    }

    @Override
    @Transactional
    public EvidenciaPagoResponse crear(EvidenciaPagoRequest request) {
        Servicio servicio = buscarServicioPagable(request.servicioId());
        controlAcceso.exigirPropietario("registrar el pago de un servicio en el que no participa",
                servicio.getCliente().getIdUsuario(), servicio.getTecnico().getUsuario().getIdUsuario());
        if (request.estadoValidacion() != null && request.estadoValidacion() != EstadoValidacion.PENDIENTE) {
            controlAcceso.exigirAdministrador("registrar un pago en estado " + request.estadoValidacion());
        }
        EvidenciaPago evidenciaPago = new EvidenciaPago();
        evidenciaPago.setServicio(servicio);
        evidenciaPago.setFechaPago(request.fechaPago() != null ? request.fechaPago() : FechaUtil.ahora());
        evidenciaPago.setEstadoValidacion(request.estadoValidacion() != null
                ? request.estadoValidacion() : EstadoValidacion.PENDIENTE);
        copiarDatos(request, evidenciaPago);
        return EvidenciaPagoResponse.desde(evidenciaPagoDao.guardar(evidenciaPago));
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
        return EvidenciaPagoResponse.desde(evidenciaPagoDao.guardar(evidenciaPago));
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        evidenciaPagoDao.eliminar(buscar(id));
    }

    private EvidenciaPago buscar(Long id) {
        return evidenciaPagoDao.buscarPorId(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("la evidencia de pago", id));
    }

    private Servicio buscarServicioPagable(Long servicioId) {
        Servicio servicio = servicioDao.buscarPorId(servicioId)
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
