package com.tecnicoya.api.security;

import com.tecnicoya.api.exception.AccesoDenegadoException;
import com.tecnicoya.api.exception.NoAutenticadoException;
import com.tecnicoya.api.model.enums.TipoUsuario;
import com.tecnicoya.api.util.Auditoria;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.cors.CorsUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Arrays;

/**
 * Exige un token JWT válido, enviado en la cabecera {@code Authorization: Bearer <token>}, antes de que la petición
 * llegue al controlador. Si falta o no es válido, responde 401.
 *
 * <p>Con el token válido, deja el id y el rol del usuario como atributos de la petición ({@link #ATRIBUTO_USUARIO},
 * {@link #ATRIBUTO_ROL}) y en el MDC de Logback, para que aparezcan en cada línea de log. Si el endpoint está
 * anotado con {@link RequiereRol} y el rol del usuario no está permitido, responde 403.
 *
 * <p>Rutas públicas, que no piden token:
 * <ul>
 *   <li>{@code POST /api/auth/login}: es donde se obtiene el token.</li>
 *   <li>{@code POST /api/usuarios}: registro de una cuenta nueva.</li>
 *   <li>{@code GET /api/archivos/{nombre}}: el navegador abre los archivos como enlaces o imágenes, sin cabeceras.</li>
 *   <li>Peticiones {@code OPTIONS} de CORS: el navegador las envía antes de la real y nunca llevan el token.</li>
 * </ul>
 * En una ruta pública el token es opcional: si viene y es válido, también se leen el usuario y el rol. Así el
 * administrador puede crear otros administradores con {@code POST /api/usuarios}, mientras que un registro
 * anónimo no puede (lo comprueba {@link ControlAcceso}).
 */
@Component
@RequiredArgsConstructor
public class JwtInterceptor implements HandlerInterceptor {

    /** Atributo de la petición con el id ({@code Long}) del usuario autenticado. */
    public static final String ATRIBUTO_USUARIO = "tecnicoya.usuarioId";
    /** Atributo de la petición con el rol ({@link TipoUsuario}) del usuario autenticado. */
    public static final String ATRIBUTO_ROL = "tecnicoya.rol";

    private static final String PREFIJO = "Bearer ";
    private static final String MDC_USUARIO = "usuario";

    private final JwtService jwtService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (esPublica(request)) {
            autenticarSiHayToken(request);
            return true;
        }
        String cabecera = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (cabecera == null || !cabecera.startsWith(PREFIJO)) {
            throw new NoAutenticadoException(
                    "Debe iniciar sesión: envíe el token en la cabecera 'Authorization: Bearer <token>'");
        }
        Claims datos;
        try {
            datos = jwtService.validarToken(cabecera.substring(PREFIJO.length()));
        } catch (JwtException | IllegalArgumentException ex) {
            Auditoria.LOG.warn("TOKEN_RECHAZADO {} {} ip={}", Auditoria.limpiar(request.getMethod()),
                    Auditoria.limpiar(request.getRequestURI()), Auditoria.limpiar(request.getRemoteAddr()));
            throw new NoAutenticadoException("El token no es válido o ya venció; inicie sesión nuevamente");
        }

        TipoUsuario rol = registrarSesion(request, datos);
        verificarRol(request, handler, (Long) request.getAttribute(ATRIBUTO_USUARIO), rol);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler,
                                Exception ex) {
        MDC.remove(MDC_USUARIO);
    }

    /** En una ruta pública el token es opcional: si no viene o no es válido, la petición sigue como anónima. */
    private void autenticarSiHayToken(HttpServletRequest request) {
        String cabecera = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (cabecera == null || !cabecera.startsWith(PREFIJO)) {
            return;
        }
        try {
            registrarSesion(request, jwtService.validarToken(cabecera.substring(PREFIJO.length())));
        } catch (JwtException | IllegalArgumentException ex) {
            // Sigue como anónima: el token es opcional en esta ruta
        }
    }

    /** Deja el id y el rol del token como atributos de la petición y en el MDC de Logback. */
    private static TipoUsuario registrarSesion(HttpServletRequest request, Claims datos) {
        Long usuarioId = Long.valueOf(datos.getSubject());
        TipoUsuario rol = TipoUsuario.valueOf(datos.get("rol", String.class));
        request.setAttribute(ATRIBUTO_USUARIO, usuarioId);
        request.setAttribute(ATRIBUTO_ROL, rol);
        MDC.put(MDC_USUARIO, usuarioId + ":" + rol);
        return rol;
    }

    private static void verificarRol(HttpServletRequest request, Object handler, Long usuarioId, TipoUsuario rol) {
        if (!(handler instanceof HandlerMethod metodo)) {
            return;
        }
        RequiereRol requerido = AnnotatedElementUtils.findMergedAnnotation(metodo.getMethod(), RequiereRol.class);
        if (requerido == null) {
            requerido = AnnotatedElementUtils.findMergedAnnotation(metodo.getBeanType(), RequiereRol.class);
        }
        if (requerido != null && Arrays.stream(requerido.value()).noneMatch(rol::equals)) {
            Auditoria.LOG.warn("ACCESO_DENEGADO usuario={} rol={} {} {}", usuarioId, rol,
                    Auditoria.limpiar(request.getMethod()), Auditoria.limpiar(request.getRequestURI()));
            throw new AccesoDenegadoException("Su rol (" + rol + ") no tiene permiso para usar este recurso");
        }
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
