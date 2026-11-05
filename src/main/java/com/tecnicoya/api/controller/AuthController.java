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

/**
 * Inicio de sesión: valida las credenciales y entrega el token JWT con los datos del usuario.
 *
 * <p>Cada intento queda en el log de auditoría (sin la contraseña) y los intentos fallidos se limitan con
 * {@link LimiteIntentosLogin} para frenar ataques de fuerza bruta.
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UsuarioService usuarioService;
    private final JwtService jwtService;
    private final LimiteIntentosLogin limiteIntentos;

    /**
     * {@code POST /api/auth/login}: valida correo y contraseña y genera el token.
     *
     * @param request correo y contraseña
     * @param http    petición HTTP, para obtener la IP del cliente
     * @return 200 con el token y los datos del usuario (sin contraseña); 400 si faltan datos;
     *         401 si las credenciales son incorrectas o la cuenta no está {@code ACTIVO};
     *         429 si se superó el número de intentos fallidos permitidos
     */
    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request, HttpServletRequest http) {
        String ip = http.getRemoteAddr();
        String clave = LimiteIntentosLogin.clave(request.correo(), ip);
        try {
            limiteIntentos.verificar(clave);
        } catch (DemasiadosIntentosException ex) {
            Auditoria.LOG.warn("LOGIN_BLOQUEADO correo={} ip={}", request.correo(), ip);
            throw ex;
        }

        UsuarioResponse usuario;
        try {
            usuario = usuarioService.autenticar(request);
        } catch (CredencialesInvalidasException ex) {
            int fallos = limiteIntentos.registrarFallo(clave);
            Auditoria.LOG.warn("LOGIN_FALLIDO correo={} ip={} intento={}/{}", request.correo(), ip, fallos,
                    limiteIntentos.getMaxIntentos());
            throw ex;
        }

        limiteIntentos.reiniciar(clave);
        Auditoria.LOG.info("LOGIN_EXITOSO usuario={} rol={} ip={}", usuario.idUsuario(), usuario.tipoUsuario(), ip);
        return new LoginResponse(jwtService.generarToken(usuario), usuario);
    }
}
