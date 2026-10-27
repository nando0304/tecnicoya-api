package com.tecnicoya.api.security;

import com.tecnicoya.api.exception.NoAutenticadoException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.cors.CorsUtils;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Exige un token JWT válido, enviado en la cabecera {@code Authorization: Bearer <token>}, antes de que la petición
 * llegue al controlador. Si falta o no es válido, responde 401.
 *
 * <p>Rutas públicas, que no piden token:
 * <ul>
 *   <li>{@code POST /api/auth/login}: es donde se obtiene el token.</li>
 *   <li>{@code POST /api/usuarios}: registro de una cuenta nueva.</li>
 *   <li>{@code GET /api/archivos/{nombre}}: el navegador abre los archivos como enlaces o imágenes, sin cabeceras.</li>
 *   <li>Peticiones {@code OPTIONS} de CORS: el navegador las envía antes de la real y nunca llevan el token.</li>
 * </ul>
 */
@Component
@RequiredArgsConstructor
public class JwtInterceptor implements HandlerInterceptor {

    private static final String PREFIJO = "Bearer ";

    private final JwtService jwtService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (esPublica(request)) {
            return true;
        }
        String cabecera = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (cabecera == null || !cabecera.startsWith(PREFIJO)) {
            throw new NoAutenticadoException(
                    "Debe iniciar sesión: envíe el token en la cabecera 'Authorization: Bearer <token>'");
        }
        try {
            jwtService.validarToken(cabecera.substring(PREFIJO.length()));
        } catch (JwtException | IllegalArgumentException ex) {
            throw new NoAutenticadoException("El token no es válido o ya venció; inicie sesión nuevamente");
        }
        return true;
    }

    private static boolean esPublica(HttpServletRequest request) {
        String metodo = request.getMethod();
        String ruta = request.getRequestURI();
        return CorsUtils.isPreFlightRequest(request)
                || ruta.equals("/api/auth/login")
                || (metodo.equals("POST") && ruta.equals("/api/usuarios"))
                || (metodo.equals("GET") && ruta.startsWith("/api/archivos/"));
    }
}
