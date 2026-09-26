package com.tecnicoya.api.dto.response;

import com.tecnicoya.api.entity.Usuario;
import com.tecnicoya.api.entity.enums.EstadoUsuario;
import com.tecnicoya.api.entity.enums.TipoUsuario;

import java.time.LocalDateTime;

/** Nunca incluye la contraseña. */
public record UsuarioResponse(
        Long idUsuario,
        String nombres,
        String apellidos,
        String correo,
        String telefono,
        TipoUsuario tipoUsuario,
        EstadoUsuario estado,
        LocalDateTime fechaRegistro
) {
    public static UsuarioResponse desde(Usuario usuario) {
        return new UsuarioResponse(
                usuario.getIdUsuario(),
                usuario.getNombres(),
                usuario.getApellidos(),
                usuario.getCorreo(),
                usuario.getTelefono(),
                usuario.getTipoUsuario(),
                usuario.getEstado(),
                usuario.getFechaRegistro());
    }
}
