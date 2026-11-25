package com.tecnicoya.api.dto.response;

public record LoginResponse(String token, UsuarioResponse usuario) {
}
