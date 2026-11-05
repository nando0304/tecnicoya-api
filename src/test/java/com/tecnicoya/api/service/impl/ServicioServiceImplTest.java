package com.tecnicoya.api.service.impl;

import com.tecnicoya.api.dao.IServicioDao;
import com.tecnicoya.api.dao.ITecnicoDao;
import com.tecnicoya.api.dao.IUsuarioDao;
import com.tecnicoya.api.dto.request.ServicioRequest;
import com.tecnicoya.api.dto.response.ServicioResponse;
import com.tecnicoya.api.exception.ReglaNegocioException;
import com.tecnicoya.api.model.enums.EstadoServicio;
import com.tecnicoya.api.model.enums.EstadoUsuario;
import com.tecnicoya.api.model.enums.EstadoVerificacion;
import com.tecnicoya.api.model.enums.Prioridad;
import com.tecnicoya.api.model.enums.TipoUsuario;
import com.tecnicoya.api.model.Servicio;
import com.tecnicoya.api.model.Tecnico;
import com.tecnicoya.api.model.Usuario;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.Mock;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
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
