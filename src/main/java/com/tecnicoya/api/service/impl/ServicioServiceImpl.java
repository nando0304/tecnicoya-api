package com.tecnicoya.api.service.impl;

import com.tecnicoya.api.dto.request.ServicioRequest;
import com.tecnicoya.api.dto.response.ServicioResponse;
import com.tecnicoya.api.entity.Servicio;
import com.tecnicoya.api.entity.Tecnico;
import com.tecnicoya.api.entity.Usuario;
import com.tecnicoya.api.entity.enums.EstadoServicio;
import com.tecnicoya.api.entity.enums.EstadoUsuario;
import com.tecnicoya.api.entity.enums.EstadoVerificacion;
import com.tecnicoya.api.entity.enums.Prioridad;
import com.tecnicoya.api.entity.enums.TipoUsuario;
import com.tecnicoya.api.exception.RecursoNoEncontradoException;
import com.tecnicoya.api.exception.ReglaNegocioException;
import com.tecnicoya.api.repository.ServicioRepository;
import com.tecnicoya.api.repository.TecnicoRepository;
import com.tecnicoya.api.repository.UsuarioRepository;
import com.tecnicoya.api.service.ServicioService;
import com.tecnicoya.api.util.FechaUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/** Implementación de {@link ServicioService}. */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ServicioServiceImpl implements ServicioService {

    private static final Set<EstadoServicio> ESTADOS_CON_TECNICO =
            EnumSet.of(EstadoServicio.ASIGNADO, EstadoServicio.EN_PROCESO, EstadoServicio.FINALIZADO);
    private static final Set<EstadoServicio> ESTADOS_CERRADOS =
            EnumSet.of(EstadoServicio.FINALIZADO, EstadoServicio.CANCELADO);

    private final ServicioRepository servicioRepository;
    private final UsuarioRepository usuarioRepository;
    private final TecnicoRepository tecnicoRepository;

    @Override
    public List<ServicioResponse> listar() {
        return servicioRepository.findAll().stream().map(ServicioResponse::desde).toList();
    }

    @Override
    public ServicioResponse obtenerPorId(Long id) {
        return ServicioResponse.desde(buscar(id));
    }

    @Override
    @Transactional
    public ServicioResponse crear(ServicioRequest request) {
        Servicio servicio = new Servicio();
        servicio.setFechaSolicitud(FechaUtil.ahora());
        if (request.estadoServicio() != null) {
            servicio.setEstadoServicio(request.estadoServicio());
        } else {
            servicio.setEstadoServicio(request.tecnicoId() != null ? EstadoServicio.ASIGNADO : EstadoServicio.PENDIENTE);
        }
        servicio.setPrioridad(request.prioridad() != null ? request.prioridad() : Prioridad.MEDIA);

        aplicarDatos(request, servicio);
        return ServicioResponse.desde(servicioRepository.save(servicio));
    }

    @Override
    @Transactional
    public ServicioResponse actualizar(Long id, ServicioRequest request) {
        Servicio servicio = buscar(id);
        if (request.estadoServicio() != null) {
            servicio.setEstadoServicio(request.estadoServicio());
        }
        if (request.prioridad() != null) {
            servicio.setPrioridad(request.prioridad());
        }

        aplicarDatos(request, servicio);
        return ServicioResponse.desde(servicioRepository.save(servicio));
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        servicioRepository.delete(buscar(id));
        servicioRepository.flush();
    }

    private Servicio buscar(Long id) {
        return servicioRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("el servicio", id));
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

        servicio.setTitulo(request.titulo().trim());
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
        Usuario cliente = usuarioRepository.findById(clienteId)
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
        Tecnico tecnico = tecnicoRepository.findById(tecnicoId)
                .orElseThrow(() -> new RecursoNoEncontradoException("el técnico", tecnicoId));
        if (tecnico.getEstadoVerificacion() != EstadoVerificacion.VERIFICADO) {
            throw new ReglaNegocioException("Solo se pueden asignar técnicos verificados; el técnico con id "
                    + tecnicoId + " está en estado " + tecnico.getEstadoVerificacion());
        }
        return tecnico;
    }
}
