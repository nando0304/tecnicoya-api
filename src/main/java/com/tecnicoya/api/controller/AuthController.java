package com.tecnicoya.api.controller;

import com.tecnicoya.api.dto.request.LoginRequest;
import com.tecnicoya.api.dto.response.UsuarioResponse;
import com.tecnicoya.api.service.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Inicio de sesión: valida las credenciales y devuelve los datos del usuario. */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UsuarioService usuarioService;

    /**
     * {@code POST /api/auth/login}: valida correo y contraseña.
     *
     * @param request correo y contraseña
     * @return 200 con los datos del usuario (sin contraseña); 400 si faltan datos;
     *         401 si las credenciales son incorrectas o la cuenta no está {@code ACTIVO}
     */
    @PostMapping("/login")
    public UsuarioResponse login(@Valid @RequestBody LoginRequest request) {
        return usuarioService.autenticar(request);
    }
}
