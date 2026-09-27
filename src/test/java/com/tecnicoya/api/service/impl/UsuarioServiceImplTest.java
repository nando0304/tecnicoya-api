package com.tecnicoya.api.service.impl;

import com.tecnicoya.api.dto.request.LoginRequest;
import com.tecnicoya.api.dto.request.UsuarioRequest;
import com.tecnicoya.api.dto.response.UsuarioResponse;
import com.tecnicoya.api.entity.Usuario;
import com.tecnicoya.api.entity.enums.EstadoUsuario;
import com.tecnicoya.api.entity.enums.TipoUsuario;
import com.tecnicoya.api.exception.ConflictoException;
import com.tecnicoya.api.exception.CredencialesInvalidasException;
import com.tecnicoya.api.exception.RecursoNoEncontradoException;
import com.tecnicoya.api.exception.ReglaNegocioException;
import com.tecnicoya.api.repository.TecnicoRepository;
import com.tecnicoya.api.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceImplTest {

    private static final String CORREO_NORMALIZADO = "ana.paredes@correo.example";

    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private TecnicoRepository tecnicoRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @InjectMocks
    private UsuarioServiceImpl usuarioService;

    @Test
    void crear_guardaContrasenaCifradaYCorreoNormalizado() {
        when(usuarioRepository.existsByCorreo(CORREO_NORMALIZADO)).thenReturn(false);
        when(passwordEncoder.encode("Secreta123")).thenReturn("$2a$10$hash");
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> {
            Usuario usuario = inv.getArgument(0);
            usuario.setIdUsuario(1L);
            return usuario;
        });

        UsuarioResponse respuesta = usuarioService.crear(request("Secreta123"));

        ArgumentCaptor<Usuario> guardado = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).save(guardado.capture());
        assertThat(guardado.getValue().getContrasena()).isEqualTo("$2a$10$hash");
        assertThat(respuesta.correo()).isEqualTo(CORREO_NORMALIZADO);
        assertThat(respuesta.estado()).isEqualTo(EstadoUsuario.ACTIVO);
        assertThat(respuesta.fechaRegistro()).isNotNull();
    }

    @Test
    void crear_conCorreoDuplicado_lanzaConflicto() {
        when(usuarioRepository.existsByCorreo(CORREO_NORMALIZADO)).thenReturn(true);

        assertThatThrownBy(() -> usuarioService.crear(request("Secreta123")))
                .isInstanceOf(ConflictoException.class)
                .hasMessageContaining(CORREO_NORMALIZADO);
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void crear_sinContrasena_lanzaReglaNegocio() {
        assertThatThrownBy(() -> usuarioService.crear(request(null)))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("contraseña");
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void obtenerPorId_inexistente_lanzaNoEncontrado() {
        when(usuarioRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> usuarioService.obtenerPorId(99L))
                .isInstanceOf(RecursoNoEncontradoException.class)
                .hasMessage("No se encontró el usuario con id 99");
    }

    @Test
    void autenticar_conCredencialesValidas_devuelveUsuario() {
        when(usuarioRepository.findByCorreo(CORREO_NORMALIZADO)).thenReturn(Optional.of(usuario(EstadoUsuario.ACTIVO)));
        when(passwordEncoder.matches("Secreta123", "$2a$10$hash")).thenReturn(true);

        UsuarioResponse respuesta = usuarioService.autenticar(login("Secreta123"));

        assertThat(respuesta.idUsuario()).isEqualTo(4L);
        assertThat(respuesta.tipoUsuario()).isEqualTo(TipoUsuario.CLIENTE);
    }

    @Test
    void autenticar_conContrasenaIncorrecta_lanzaCredencialesInvalidas() {
        when(usuarioRepository.findByCorreo(CORREO_NORMALIZADO)).thenReturn(Optional.of(usuario(EstadoUsuario.ACTIVO)));
        when(passwordEncoder.matches("Otra12345", "$2a$10$hash")).thenReturn(false);

        assertThatThrownBy(() -> usuarioService.autenticar(login("Otra12345")))
                .isInstanceOf(CredencialesInvalidasException.class)
                .hasMessage("Correo o contraseña incorrectos");
    }

    @Test
    void autenticar_conCorreoInexistente_lanzaElMismoMensaje() {
        when(usuarioRepository.findByCorreo(CORREO_NORMALIZADO)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> usuarioService.autenticar(login("Secreta123")))
                .isInstanceOf(CredencialesInvalidasException.class)
                .hasMessage("Correo o contraseña incorrectos");
    }

    @Test
    void autenticar_conCuentaBloqueada_lanzaCredencialesInvalidas() {
        when(usuarioRepository.findByCorreo(CORREO_NORMALIZADO)).thenReturn(Optional.of(usuario(EstadoUsuario.BLOQUEADO)));
        when(passwordEncoder.matches("Secreta123", "$2a$10$hash")).thenReturn(true);

        assertThatThrownBy(() -> usuarioService.autenticar(login("Secreta123")))
                .isInstanceOf(CredencialesInvalidasException.class)
                .hasMessageContaining("BLOQUEADO");
    }

    private static Usuario usuario(EstadoUsuario estado) {
        Usuario usuario = new Usuario();
        usuario.setIdUsuario(4L);
        usuario.setNombres("Ana");
        usuario.setApellidos("Paredes");
        usuario.setCorreo(CORREO_NORMALIZADO);
        usuario.setContrasena("$2a$10$hash");
        usuario.setTipoUsuario(TipoUsuario.CLIENTE);
        usuario.setEstado(estado);
        return usuario;
    }

    private static LoginRequest login(String contrasena) {
        return new LoginRequest("  Ana.Paredes@Correo.Example ", contrasena);
    }

    private static UsuarioRequest request(String contrasena) {
        return new UsuarioRequest("Ana", "Paredes", "  Ana.Paredes@Correo.Example ", contrasena,
                "987654321", TipoUsuario.CLIENTE, null);
    }
}
