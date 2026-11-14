package com.tecnicoya.api.service.impl;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.tecnicoya.api.dao.IServicioDao;
import com.tecnicoya.api.dao.ITecnicoDao;
import com.tecnicoya.api.dao.IUsuarioDao;
import com.tecnicoya.api.dto.request.ServicioRequest;
import com.tecnicoya.api.dto.response.ServicioResponse;
import com.tecnicoya.api.exception.RecursoNoEncontradoException;
import com.tecnicoya.api.exception.ReglaNegocioException;
import com.tecnicoya.api.model.enums.EstadoServicio;
import com.tecnicoya.api.model.enums.EstadoUsuario;
import com.tecnicoya.api.model.enums.EstadoVerificacion;
import com.tecnicoya.api.model.enums.Prioridad;
import com.tecnicoya.api.model.enums.TipoUsuario;
import com.tecnicoya.api.model.Servicio;
import com.tecnicoya.api.model.Tecnico;
import com.tecnicoya.api.model.Usuario;
import com.tecnicoya.api.security.ControlAcceso;
import com.tecnicoya.api.service.ServicioService;
import com.tecnicoya.api.util.FechaUtil;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Implementación de {@link ServicioService}.
 *
 * <p>Control de acceso ({@link ControlAcceso}): un cliente solo solicita servicios a su nombre y solo edita los
 * suyos; un técnico solo edita los servicios que tiene asignados o acepta uno libre. Solo el técnico asignado
 * marca el trabajo como EN_PROCESO o FINALIZADO (así un cliente no puede cerrar un servicio y calificarlo).
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ServicioServiceImpl implements ServicioService {

    /** Conjuntos inmutables de Google Guava (respaldados internamente por un EnumSet). */
    private static final ImmutableSet<EstadoServicio> ESTADOS_CON_TECNICO =
            Sets.immutableEnumSet(EstadoServicio.ASIGNADO, EstadoServicio.EN_PROCESO, EstadoServicio.FINALIZADO);
    private static final ImmutableSet<EstadoServicio> ESTADOS_CERRADOS =
            Sets.immutableEnumSet(EstadoServicio.FINALIZADO, EstadoServicio.CANCELADO);
    /** Estados con los que un cliente puede registrar una solicitud. */
    private static final ImmutableSet<EstadoServicio> ESTADOS_INICIALES =
            Sets.immutableEnumSet(EstadoServicio.PENDIENTE, EstadoServicio.ASIGNADO);
    /** Estados que solo marca el técnico asignado (o el administrador). */
    private static final ImmutableSet<EstadoServicio> ESTADOS_DEL_TECNICO =
            Sets.immutableEnumSet(EstadoServicio.EN_PROCESO, EstadoServicio.FINALIZADO);

    private final IServicioDao servicioDao;
    private final IUsuarioDao usuarioDao;
    private final ITecnicoDao tecnicoDao;
    private final ControlAcceso controlAcceso;

    @Override
    public List<ServicioResponse> listar() {
        return servicioDao.listar().stream().map(ServicioResponse::desde).toList();
    }

    @Override
    public ServicioResponse obtenerPorId(Long id) {
        return ServicioResponse.desde(buscar(id));
    }

    @Override
    @Transactional
    public ServicioResponse crear(ServicioRequest request) {
        controlAcceso.exigirPropietario("solicitar un servicio a nombre de otro cliente", request.clienteId());
        if (request.estadoServicio() != null && !ESTADOS_INICIALES.contains(request.estadoServicio())) {
            controlAcceso.exigirAdministrador("registrar un servicio en estado " + request.estadoServicio());
        }
        Servicio servicio = new Servicio();
        servicio.setFechaSolicitud(FechaUtil.ahora());
        if (request.estadoServicio() != null) {
            servicio.setEstadoServicio(request.estadoServicio());
        } else {
            servicio.setEstadoServicio(request.tecnicoId() != null ? EstadoServicio.ASIGNADO : EstadoServicio.PENDIENTE);
        }
        servicio.setPrioridad(request.prioridad() != null ? request.prioridad() : Prioridad.MEDIA);

        aplicarDatos(request, servicio);
        return ServicioResponse.desde(servicioDao.guardar(servicio));
    }

    @Override
    @Transactional
    public ServicioResponse actualizar(Long id, ServicioRequest request) {
        Servicio servicio = buscar(id);
        verificarPermisoDeEdicion(servicio, request);
        if (request.estadoServicio() != null) {
            servicio.setEstadoServicio(request.estadoServicio());
        }
        if (request.prioridad() != null) {
            servicio.setPrioridad(request.prioridad());
        }

        aplicarDatos(request, servicio);
        return ServicioResponse.desde(servicioDao.guardar(servicio));
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        servicioDao.eliminar(buscar(id));
    }

    private Servicio buscar(Long id) {
        return servicioDao.buscarPorId(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("el servicio", id));
    }

    private void verificarPermisoDeEdicion(Servicio servicio, ServicioRequest request) {
        Long cliente = servicio.getCliente().getIdUsuario();
        Long tecnicoAsignado = usuarioDe(servicio.getTecnico());
        Long tecnicoSolicitado = request.tecnicoId() == null ? null
                : tecnicoDao.buscarPorId(request.tecnicoId()).map(ServicioServiceImpl::usuarioDe).orElse(null);
        // Un técnico puede tomar un servicio que no tiene técnico (aceptar la solicitud)
        Long tecnicoQueAcepta = servicio.getTecnico() == null ? tecnicoSolicitado : null;

        controlAcceso.exigirPropietario("modificar un servicio de otro usuario",
                cliente, tecnicoAsignado, tecnicoQueAcepta);
        if (!cliente.equals(request.clienteId())) {
            controlAcceso.exigirAdministrador("cambiar el cliente de un servicio");
        }
        EstadoServicio nuevoEstado = request.estadoServicio();
        boolean cambiaEstado = nuevoEstado != null && nuevoEstado != servicio.getEstadoServicio();
        if (cambiaEstado && ESTADOS_DEL_TECNICO.contains(nuevoEstado)) {
            controlAcceso.exigirPropietario(
                    "marcar el servicio como " + nuevoEstado + "; solo lo hace el técnico asignado", tecnicoSolicitado);
        }
    }

    private static Long usuarioDe(Tecnico tecnico) {
        return tecnico == null ? null : tecnico.getUsuario().getIdUsuario();
    }

    private void aplicarDatos(ServicioRequest request, Servicio servicio) {
        // El cliente y el técnico solo se revalidan cuando cambian
        if (servicio.getCliente() == null || !servicio.getCliente().getIdUsuario().equals(request.clienteId())) {
            servicio.setCliente(buscarClienteActivo(request.clienteId()));
        }
        if (request.tecnicoId() == null) {
            servicio.setTecnico(null);
        } else if (servicio.getTecnico() == null || !servicio.getTecnico().getIdTecnico().equals(request.tecnicoId())) {
            servicio.setTecnico(buscarTecnicoVerificado(request.tecnicoId()));
        }

        servicio.setTitulo(StringUtils.normalizeSpace(request.titulo()));
        servicio.setDescripcionProblema(request.descripcionProblema().trim());
        servicio.setFechaServicio(request.fechaServicio());
        validarEstadoYFechas(servicio, request.fechaCierre());
    }

    private void validarEstadoYFechas(Servicio servicio, LocalDateTime fechaCierreSolicitada) {
        EstadoServicio estado = servicio.getEstadoServicio();
        if (servicio.getTecnico() == null && ESTADOS_CON_TECNICO.contains(estado)) {
            throw new ReglaNegocioException("Un servicio en estado " + estado + " debe tener un técnico asignado");
        }
        if (servicio.getFechaServicio() != null && servicio.getFechaServicio().isBefore(servicio.getFechaSolicitud())) {
            throw new ReglaNegocioException("La fecha del servicio no puede ser anterior a la fecha de solicitud");
        }

        if (ESTADOS_CERRADOS.contains(estado)) {
            LocalDateTime cierre = fechaCierreSolicitada != null ? fechaCierreSolicitada
                    : servicio.getFechaCierre() != null ? servicio.getFechaCierre()
                    : FechaUtil.ahora();
            if (cierre.isBefore(servicio.getFechaSolicitud())) {
                throw new ReglaNegocioException("La fecha de cierre no puede ser anterior a la fecha de solicitud");
            }
            servicio.setFechaCierre(cierre);
        } else {
            if (fechaCierreSolicitada != null) {
                throw new ReglaNegocioException(
                        "La fecha de cierre solo se registra cuando el servicio está FINALIZADO o CANCELADO");
            }
            servicio.setFechaCierre(null);
        }
    }

    private Usuario buscarClienteActivo(Long clienteId) {
        Usuario cliente = usuarioDao.buscarPorId(clienteId)
                .orElseThrow(() -> new RecursoNoEncontradoException("el cliente", clienteId));
        if (cliente.getTipoUsuario() != TipoUsuario.CLIENTE) {
            throw new ReglaNegocioException("El usuario con id " + clienteId + " no es de tipo CLIENTE");
        }
        if (cliente.getEstado() != EstadoUsuario.ACTIVO) {
            throw new ReglaNegocioException("El cliente con id " + clienteId + " no está activo");
        }
        return cliente;
    }

    private Tecnico buscarTecnicoVerificado(Long tecnicoId) {
        Tecnico tecnico = tecnicoDao.buscarPorId(tecnicoId)
                .orElseThrow(() -> new RecursoNoEncontradoException("el técnico", tecnicoId));
        if (tecnico.getEstadoVerificacion() != EstadoVerificacion.VERIFICADO) {
            throw new ReglaNegocioException("Solo se pueden asignar técnicos verificados; el técnico con id "
                    + tecnicoId + " está en estado " + tecnico.getEstadoVerificacion());
        }
        return tecnico;
    }
}
