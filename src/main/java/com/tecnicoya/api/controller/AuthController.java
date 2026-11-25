package com.tecnicoya.api.controller;

import com.tecnicoya.api.dto.request.LoginRequest;
import com.tecnicoya.api.dto.response.LoginResponse;
import com.tecnicoya.api.dto.response.UsuarioResponse;
import com.tecnicoya.api.exception.CredencialesInvalidasException;
import com.tecnicoya.api.exception.DemasiadosIntentosException;
import com.tecnicoya.api.security.JwtService;
import com.tecnicoya.api.security.LimiteIntentosLogin;
import com.tecnicoya.api.service.UsuarioService;
import com.tecnicoya.api.util.Auditoria;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final UsuarioService usuarioService;
    private final JwtService jwtService;
    private final LimiteIntentosLogin limiteIntentos;

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request, HttpServletRequest http) {
        String ip = http.getRemoteAddr();
        String clave = LimiteIntentosLogin.clave(request.correo(), ip);
        try {
            limiteIntentos.verificar(clave);
        } catch (DemasiadosIntentosException ex) {
            Auditoria.LOG.warn("LOGIN_BLOQUEADO correo={} ip={}", Auditoria.limpiar(request.correo()),
                    Auditoria.limpiar(ip));
            throw ex;
        }

        UsuarioResponse usuario;
        try {
            usuario = usuarioService.autenticar(request);
        } catch (CredencialesInvalidasException ex) {
            int fallos = limiteIntentos.registrarFallo(clave);
            Auditoria.LOG.warn("LOGIN_FALLIDO correo={} ip={} intento={}/{}", Auditoria.limpiar(request.correo()),
                    Auditoria.limpiar(ip), fallos,
                    limiteIntentos.getMaxIntentos());
            throw ex;
        }

        limiteIntentos.reiniciar(clave);
        Auditoria.LOG.info("LOGIN_EXITOSO usuario={} rol={} ip={}", usuario.idUsuario(), usuario.tipoUsuario(),
                Auditoria.limpiar(ip));
        return new LoginResponse(jwtService.generarToken(usuario), usuario);
    }
}
