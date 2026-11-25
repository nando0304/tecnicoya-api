package com.tecnicoya.api.dto.request;

import com.tecnicoya.api.model.enums.EstadoUsuario;
import com.tecnicoya.api.model.enums.TipoUsuario;
import com.tecnicoya.api.validation.SinHtml;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UsuarioRequest(
        @NotBlank(message = "Los nombres son obligatorios")
        @Size(max = 100, message = "Los nombres no deben superar los 100 caracteres")
        @SinHtml
        String nombres,

        @NotBlank(message = "Los apellidos son obligatorios")
        @Size(max = 100, message = "Los apellidos no deben superar los 100 caracteres")
        @SinHtml
        String apellidos,

        @NotBlank(message = "El correo es obligatorio")
        @Email(message = "El correo no tiene un formato válido")
        @Size(max = 150, message = "El correo no debe superar los 150 caracteres")
        String correo,

        @Size(min = 8, max = 72, message = "La contraseña debe tener entre 8 y 72 caracteres")
        @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d).*$",
                message = "La contraseña debe contener al menos una letra y un número")
        String contrasena,

        @Pattern(regexp = "^\\+?[0-9]{7,15}$",
                message = "El teléfono debe tener entre 7 y 15 dígitos, opcionalmente precedido de '+'")
        String telefono,

        @NotNull(message = "El tipo de usuario es obligatorio")
        TipoUsuario tipoUsuario,

        EstadoUsuario estado
) {
}
