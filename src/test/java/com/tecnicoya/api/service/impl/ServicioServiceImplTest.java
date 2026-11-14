package com.tecnicoya.api.service.impl;

import com.tecnicoya.api.dao.IServicioDao;
import com.tecnicoya.api.dao.ITecnicoDao;
import com.tecnicoya.api.dao.IUsuarioDao;
import com.tecnicoya.api.dto.request.ServicioRequest;
import com.tecnicoya.api.dto.response.ServicioResponse;
import com.tecnicoya.api.exception.AccesoDenegadoException;
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
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.Mock;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.startsWith;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ServicioServiceImplTest {

    @Mock
    private IServicioDao servicioDao;
    @Mock
    private IUsuarioDao usuarioDao;
    @Mock
    private ITecnicoDao tecnicoDao;
    @Mock
    private ControlAcceso controlAcceso;
    @InjectMocks
    private ServicioServiceImpl servicioService;

    @Test
    void crear_conTecnicoYSinEstado_quedaAsignadoConPrioridadMedia() {
        when(usuarioDao.buscarPorId(2L)).thenReturn(Optional.of(usuario(2L, TipoUsuario.CLIENTE)));
        when(tecnicoDao.buscarPorId(1L)).thenReturn(Optional.of(tecnico(EstadoVerificacion.VERIFICADO)));
        when(servicioDao.guardar(any(Servicio.class))).thenAnswer(inv -> inv.getArgument(0));

        ServicioResponse respuesta = servicioService.crear(request(1L, null));

        assertThat(respuesta.estadoServicio()).isEqualTo(EstadoServicio.ASIGNADO);
        assertThat(respuesta.prioridad()).isEqualTo(Prioridad.MEDIA);
        assertThat(respuesta.fechaSolicitud()).isNotNull();
        assertThat(respuesta.fechaCierre()).isNull();
    }

    @Test
    void crear_finalizadoSinFechaCierre_asignaFechaCierreAutomatica() {
        when(usuarioDao.buscarPorId(2L)).thenReturn(Optional.of(usuario(2L, TipoUsuario.CLIENTE)));
        when(tecnicoDao.buscarPorId(1L)).thenReturn(Optional.of(tecnico(EstadoVerificacion.VERIFICADO)));
        when(servicioDao.guardar(any(Servicio.class))).thenAnswer(inv -> inv.getArgument(0));

        ServicioResponse respuesta = servicioService.crear(request(1L, EstadoServicio.FINALIZADO));

        assertThat(respuesta.fechaCierre()).isNotNull().isAfterOrEqualTo(respuesta.fechaSolicitud());
    }

    @Test
    void crear_conUsuarioQueNoEsCliente_lanzaReglaNegocio() {
        when(usuarioDao.buscarPorId(2L)).thenReturn(Optional.of(usuario(2L, TipoUsuario.TECNICO)));

        assertThatThrownBy(() -> servicioService.crear(request(null, null)))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("no es de tipo CLIENTE");
        verify(servicioDao, never()).guardar(any());
    }

    @Test
    void crear_enProcesoSinTecnico_lanzaReglaNegocio() {
        when(usuarioDao.buscarPorId(2L)).thenReturn(Optional.of(usuario(2L, TipoUsuario.CLIENTE)));

        assertThatThrownBy(() -> servicioService.crear(request(null, EstadoServicio.EN_PROCESO)))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("debe tener un técnico asignado");
        verify(servicioDao, never()).guardar(any());
    }

    @Test
    void crear_conTecnicoNoVerificado_lanzaReglaNegocio() {
        when(usuarioDao.buscarPorId(2L)).thenReturn(Optional.of(usuario(2L, TipoUsuario.CLIENTE)));
        when(tecnicoDao.buscarPorId(1L)).thenReturn(Optional.of(tecnico(EstadoVerificacion.PENDIENTE)));

        assertThatThrownBy(() -> servicioService.crear(request(1L, null)))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("técnicos verificados");
    }

    @Test
    void crear_aNombreDeOtroCliente_lanzaAccesoDenegadoSinGuardar() {
        doThrow(new AccesoDenegadoException("No tiene permiso"))
                .when(controlAcceso).exigirPropietario(anyString(), eq(2L));

        assertThatThrownBy(() -> servicioService.crear(request(null, null)))
                .isInstanceOf(AccesoDenegadoException.class);
        verify(servicioDao, never()).guardar(any());
    }

    @Test
    void crear_clienteNoPuedeRegistrarloYaFinalizado() {
        doThrow(new AccesoDenegadoException("No tiene permiso"))
                .when(controlAcceso).exigirAdministrador(anyString());

        assertThatThrownBy(() -> servicioService.crear(request(1L, EstadoServicio.FINALIZADO)))
                .isInstanceOf(AccesoDenegadoException.class);
        verify(servicioDao, never()).guardar(any());
    }

    @Test
    void actualizar_aFinalizado_exigeQueSeaElTecnicoAsignado() {
        Servicio existente = new Servicio();
        existente.setIdServicio(9L);
        existente.setCliente(usuario(2L, TipoUsuario.CLIENTE));
        existente.setTecnico(tecnico(EstadoVerificacion.VERIFICADO));
        existente.setEstadoServicio(EstadoServicio.EN_PROCESO);
        existente.setFechaSolicitud(LocalDateTime.now().minusDays(1));
        when(servicioDao.buscarPorId(9L)).thenReturn(Optional.of(existente));
        when(tecnicoDao.buscarPorId(1L)).thenReturn(Optional.of(existente.getTecnico()));
        // La primera verificación (participantes del servicio) pasa; la segunda pide al técnico asignado (usuario 5)
        lenient().doThrow(new AccesoDenegadoException("solo lo hace el técnico asignado"))
                .when(controlAcceso).exigirPropietario(startsWith("marcar el servicio"), eq(5L));

        assertThatThrownBy(() -> servicioService.actualizar(9L, request(1L, EstadoServicio.FINALIZADO)))
                .isInstanceOf(AccesoDenegadoException.class)
                .hasMessageContaining("técnico asignado");
        verify(servicioDao, never()).guardar(any());
    }

    private static ServicioRequest request(Long tecnicoId, EstadoServicio estado) {
        return new ServicioRequest(2L, tecnicoId, "Fuga de agua", "Gotea la tubería del lavadero",
                estado, null, null, null);
    }

    private static Usuario usuario(Long id, TipoUsuario tipo) {
        Usuario usuario = new Usuario();
        usuario.setIdUsuario(id);
        usuario.setNombres("María");
        usuario.setApellidos("Quispe");
        usuario.setTipoUsuario(tipo);
        usuario.setEstado(EstadoUsuario.ACTIVO);
        return usuario;
    }

    private static Tecnico tecnico(EstadoVerificacion estadoVerificacion) {
        Tecnico tecnico = new Tecnico();
        tecnico.setIdTecnico(1L);
        tecnico.setUsuario(usuario(5L, TipoUsuario.TECNICO));
        tecnico.setEspecialidad("Plomería");
        tecnico.setEstadoVerificacion(estadoVerificacion);
        return tecnico;
    }
}
