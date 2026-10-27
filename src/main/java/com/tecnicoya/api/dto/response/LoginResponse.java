package com.tecnicoya.api.dto.response;

/**
 * Respuesta del inicio de sesión.
 *
 * @param token   JWT que debe enviarse en la cabecera {@code Authorization: Bearer <token>} de las demás peticiones
 * @param usuario datos del usuario que inició sesión (sin contraseña)
 */
public record LoginResponse(String token, UsuarioResponse usuario) {
}
