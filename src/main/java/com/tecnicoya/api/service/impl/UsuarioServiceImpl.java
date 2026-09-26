package com.tecnicoya.api.service.impl;

import com.tecnicoya.api.dto.request.UsuarioRequest;
import com.tecnicoya.api.dto.response.UsuarioResponse;
import com.tecnicoya.api.entity.Usuario;
import com.tecnicoya.api.entity.enums.EstadoUsuario;
import com.tecnicoya.api.entity.enums.TipoUsuario;
import com.tecnicoya.api.exception.ConflictoException;
import com.tecnicoya.api.exception.RecursoNoEncontradoException;
import com.tecnicoya.api.exception.ReglaNegocioException;
import com.tecnicoya.api.repository.TecnicoRepository;
import com.tecnicoya.api.repository.UsuarioRepository;
import com.tecnicoya.api.service.UsuarioService;
import com.tecnicoya.api.util.FechaUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UsuarioServiceImpl implements UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final TecnicoRepository tecnicoRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public List<UsuarioResponse> listar() {
        return usuarioRepository.findAll().stream().map(UsuarioResponse::desde).toList();
    }

    @Override
    public UsuarioResponse obtenerPorId(Long id) {
        return UsuarioResponse.desde(buscar(id));
    }

    @Override
    @Transactional
    public UsuarioResponse crear(UsuarioRequest request) {
        if (!StringUtils.hasText(request.contrasena())) {
            throw new ReglaNegocioException("La contraseña es obligatoria al registrar un usuario");
        }
        String correo = normalizarCorreo(request.correo());
        if (usuarioRepository.existsByCorreo(correo)) {
            throw new ConflictoException("Ya existe un usuario registrado con el correo " + correo);
        }

        Usuario usuario = new Usuario();
        copiarDatos(request, usuario, correo);
        usuario.setContrasena(passwordEncoder.encode(request.contrasena()));
        usuario.setEstado(request.estado() != null ? request.estado() : EstadoUsuario.ACTIVO);
        usuario.setFechaRegistro(FechaUtil.ahora());
        return UsuarioResponse.desde(usuarioRepository.save(usuario));
    }

    @Override
    @Transactional
    public UsuarioResponse actualizar(Long id, UsuarioRequest request) {
        Usuario usuario = buscar(id);
        String correo = normalizarCorreo(request.correo());
        if (usuarioRepository.existsByCorreoAndIdUsuarioNot(correo, id)) {
            throw new ConflictoException("Ya existe otro usuario registrado con el correo " + correo);
        }
        if (request.tipoUsuario() != TipoUsuario.TECNICO && tecnicoRepository.existsByUsuario_IdUsuario(id)) {
            throw new ReglaNegocioException("El usuario tiene un perfil de técnico asociado; no puede cambiar su tipo a "
                    + request.tipoUsuario());
        }

        copiarDatos(request, usuario, correo);
        if (StringUtils.hasText(request.contrasena())) {
            usuario.setContrasena(passwordEncoder.encode(request.contrasena()));
        }
        if (request.estado() != null) {
            usuario.setEstado(request.estado());
        }
        return UsuarioResponse.desde(usuarioRepository.save(usuario));
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        usuarioRepository.delete(buscar(id));
        usuarioRepository.flush();
    }

    private Usuario buscar(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("el usuario", id));
    }

    private static void copiarDatos(UsuarioRequest request, Usuario usuario, String correo) {
        usuario.setNombres(request.nombres().trim());
        usuario.setApellidos(request.apellidos().trim());
        usuario.setCorreo(correo);
        usuario.setTelefono(request.telefono());
        usuario.setTipoUsuario(request.tipoUsuario());
    }

    private static String normalizarCorreo(String correo) {
        return correo.trim().toLowerCase(Locale.ROOT);
    }
}
