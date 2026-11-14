package com.tecnicoya.api.service.impl;

import com.tecnicoya.api.dao.ITecnicoDao;
import com.tecnicoya.api.dao.IUsuarioDao;
import com.tecnicoya.api.dto.request.LoginRequest;
import com.tecnicoya.api.dto.request.UsuarioRequest;
import com.tecnicoya.api.dto.response.UsuarioResponse;
import com.tecnicoya.api.exception.ConflictoException;
import com.tecnicoya.api.exception.CredencialesInvalidasException;
import com.tecnicoya.api.exception.RecursoNoEncontradoException;
import com.tecnicoya.api.exception.ReglaNegocioException;
import com.tecnicoya.api.model.enums.EstadoUsuario;
import com.tecnicoya.api.model.enums.TipoUsuario;
import com.tecnicoya.api.model.Usuario;
import com.tecnicoya.api.security.ControlAcceso;
import com.tecnicoya.api.service.UsuarioService;
import com.tecnicoya.api.util.FechaUtil;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

/**
 * Implementación de {@link UsuarioService}. Registra en el log (Logback) las altas, bajas y cambios de contraseña.
 *
 * <p>Control de acceso ({@link ControlAcceso}): cada usuario solo ve y edita su propia cuenta, y solo un
 * administrador puede crear administradores, cambiar el tipo de un usuario o su estado.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UsuarioServiceImpl implements UsuarioService {

    private final IUsuarioDao usuarioDao;
    private final ITecnicoDao tecnicoDao;
    private final PasswordEncoder passwordEncoder;
    private final ControlAcceso controlAcceso;

    @Override
    public List<UsuarioResponse> listar() {
        return usuarioDao.listar().stream().map(UsuarioResponse::desde).toList();
    }

    @Override
    public UsuarioResponse obtenerPorId(Long id) {
        controlAcceso.exigirPropietario("ver los datos de otro usuario", id);
        return UsuarioResponse.desde(buscar(id));
    }

    /** Mismo mensaje para correo inexistente y contraseña errónea: no revela qué correos están registrados. */
    @Override
    public UsuarioResponse autenticar(LoginRequest request) {
        Usuario usuario = usuarioDao.buscarPorCorreo(normalizarCorreo(request.correo()))
                .filter(encontrado -> passwordEncoder.matches(request.contrasena(), encontrado.getContrasena()))
                .orElseThrow(() -> new CredencialesInvalidasException("Correo o contraseña incorrectos"));
        if (usuario.getEstado() != EstadoUsuario.ACTIVO) {
            throw new CredencialesInvalidasException("La cuenta está " + usuario.getEstado()
                    + "; comuníquese con el administrador");
        }
        return UsuarioResponse.desde(usuario);
    }

    @Override
    @Transactional
    public UsuarioResponse crear(UsuarioRequest request) {
        if (StringUtils.isBlank(request.contrasena())) {
            throw new ReglaNegocioException("La contraseña es obligatoria al registrar un usuario");
        }
        // Registro público: nadie puede crearse una cuenta de administrador ni elegir otro estado que ACTIVO
        if (request.tipoUsuario() == TipoUsuario.ADMINISTRADOR) {
            controlAcceso.exigirAdministrador("registrar un usuario ADMINISTRADOR");
        }
        if (request.estado() != null && request.estado() != EstadoUsuario.ACTIVO) {
            controlAcceso.exigirAdministrador("registrar un usuario en estado " + request.estado());
        }
        String correo = normalizarCorreo(request.correo());
        if (usuarioDao.existeCorreo(correo)) {
            throw new ConflictoException("Ya existe un usuario registrado con el correo " + correo);
        }

        Usuario usuario = new Usuario();
        copiarDatos(request, usuario, correo);
        usuario.setContrasena(passwordEncoder.encode(request.contrasena()));
        usuario.setEstado(request.estado() != null ? request.estado() : EstadoUsuario.ACTIVO);
        usuario.setFechaRegistro(FechaUtil.ahora());
        Usuario guardado = usuarioDao.guardar(usuario);
        log.info("Usuario registrado: id={}, tipo={}", guardado.getIdUsuario(), guardado.getTipoUsuario());
        return UsuarioResponse.desde(guardado);
    }

    @Override
    @Transactional
    public UsuarioResponse actualizar(Long id, UsuarioRequest request) {
        controlAcceso.exigirPropietario("modificar los datos de otro usuario", id);
        Usuario usuario = buscar(id);
        if (request.tipoUsuario() != usuario.getTipoUsuario()) {
            controlAcceso.exigirAdministrador("cambiar el tipo de usuario a " + request.tipoUsuario());
        }
        if (request.estado() != null && request.estado() != usuario.getEstado()) {
            controlAcceso.exigirAdministrador("cambiar el estado de la cuenta a " + request.estado());
        }
        String correo = normalizarCorreo(request.correo());
        if (usuarioDao.existeCorreoEnOtroUsuario(correo, id)) {
            throw new ConflictoException("Ya existe otro usuario registrado con el correo " + correo);
        }
        if (request.tipoUsuario() != TipoUsuario.TECNICO && tecnicoDao.existePerfilDeUsuario(id)) {
            throw new ReglaNegocioException("El usuario tiene un perfil de técnico asociado; no puede cambiar su tipo a "
                    + request.tipoUsuario());
        }

        copiarDatos(request, usuario, correo);
        if (StringUtils.isNotBlank(request.contrasena())) {
            usuario.setContrasena(passwordEncoder.encode(request.contrasena()));
            log.info("Contraseña cambiada: usuario id={}", id);
        }
        if (request.estado() != null) {
            usuario.setEstado(request.estado());
        }
        return UsuarioResponse.desde(usuarioDao.guardar(usuario));
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        usuarioDao.eliminar(buscar(id));
        log.info("Usuario eliminado: id={}", id);
    }

    private Usuario buscar(Long id) {
        return usuarioDao.buscarPorId(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("el usuario", id));
    }

    private static void copiarDatos(UsuarioRequest request, Usuario usuario, String correo) {
        usuario.setNombres(StringUtils.normalizeSpace(request.nombres()));
        usuario.setApellidos(StringUtils.normalizeSpace(request.apellidos()));
        usuario.setCorreo(correo);
        usuario.setTelefono(request.telefono());
        usuario.setTipoUsuario(request.tipoUsuario());
    }

    private static String normalizarCorreo(String correo) {
        return correo.trim().toLowerCase(Locale.ROOT);
    }
}
