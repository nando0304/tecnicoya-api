package com.tecnicoya.api.dto.response;

import com.tecnicoya.api.entity.Tecnico;
import com.tecnicoya.api.entity.Usuario;
import com.tecnicoya.api.entity.enums.EstadoVerificacion;

/** Perfil de técnico devuelto por la API, junto con los datos de contacto de su usuario. */
public record TecnicoResponse(
        Long idTecnico,
        Long usuarioId,
        String nombreCompleto,
        String correo,
        String telefono,
        String especialidad,
        String descripcion,
        EstadoVerificacion estadoVerificacion
) {
    public static TecnicoResponse desde(Tecnico tecnico) {
        Usuario usuario = tecnico.getUsuario();
        return new TecnicoResponse(
                tecnico.getIdTecnico(),
                usuario.getIdUsuario(),
                usuario.nombreCompleto(),
                usuario.getCorreo(),
                usuario.getTelefono(),
                tecnico.getEspecialidad(),
                tecnico.getDescripcion(),
                tecnico.getEstadoVerificacion());
    }
}
