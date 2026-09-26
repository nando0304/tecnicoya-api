package com.tecnicoya.api.dto.request;

import com.tecnicoya.api.entity.enums.EstadoUsuario;
import com.tecnicoya.api.entity.enums.TipoUsuario;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Datos para registrar o actualizar un usuario.
 * La contraseña es obligatoria al crear; al actualizar, si se omite se conserva la actual.
 */
public record UsuarioRequest(

        @NotBlank(message = "Los nombres son obligatorios")
        @Size(max = 100, message = "Los nombres no deben superar los 100 caracteres")
        String nombres,

        @NotBlank(message = "Los apellidos son obligatorios")
        @Size(max = 100, message = "Los apellidos no deben superar los 100 caracteres")
        String apellidos,

        @NotBlank(message = "El correo es obligatorio")
        @Email(message = "El correo no tiene un formato válido")
        @Size(max = 150, message = "El correo no debe superar los 150 caracteres")
        String correo,

        @Size(min = 8, max = 72, message = "La contraseña debe tener entre 8 y 72 caracteres")
        String contrasena,

        @Pattern(regexp = "^\\+?[0-9]{7,15}$",
                message = "El teléfono debe tener entre 7 y 15 dígitos, opcionalmente precedido de '+'")
        String telefono,

        @NotNull(message = "El tipo de usuario es obligatorio")
        TipoUsuario tipoUsuario,

        EstadoUsuario estado
) {
}
