package com.tecnicoya.api.security;

import com.tecnicoya.api.exception.AccesoDenegadoException;
import com.tecnicoya.api.exception.NoAutenticadoException;
import com.tecnicoya.api.model.enums.TipoUsuario;
import com.tecnicoya.api.util.Auditoria;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.Principal;

/**
 * Protege los endpoints de monitoreo de Spring Boot Actuator ({@code /actuator/**}), que muestran métricas internas
 * del servidor y permiten cambiar el nivel de los logs.
 *
 * <p>{@link JwtInterceptor} no los alcanza (Actuator no usa los controladores de Spring MVC), por eso este filtro se
 * ejecuta antes:
 * <ul>
 *   <li>{@code GET /actuator/health}: público, para que un balanceador o Docker sepa si la API está viva. Un anónimo
 *       solo ve {@code {"status":"UP"}}; el ADMINISTRADOR ve además el detalle (base de datos, disco).</li>
 *   <li>El resto ({@code metrics}, {@code prometheus}, {@code info}, {@code loggers}): exige el token JWT de un
 *       ADMINISTRADOR. Sin token responde 401 y con otro rol 403; ambos quedan en el log de auditoría.</li>
 *   <li>Prometheus no puede iniciar sesión: usa el token fijo {@code tecnicoya.monitoreo.token} (variable
 *       {@code MONITOREO_TOKEN}), que solo permite peticiones GET (lectura). Se compara en tiempo constante.</li>
 * </ul>
 * Los errores se responden con el mismo JSON que el resto de la API, delegando en {@code GlobalExceptionHandler}.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
public class MonitoreoSeguridadFilter extends OncePerRequestFilter {

    private static final String PREFIJO_ACTUATOR = "/actuator";
    private static final String RUTA_SALUD = "/actuator/health";
    private static final String PREFIJO_TOKEN = "Bearer ";
    private static final String MDC_USUARIO = "usuario";
    private static final String USUARIO_PROMETHEUS = "monitoreo";

    private final JwtService jwtService;
    private final HandlerExceptionResolver resolverErrores;
    private final byte[] tokenMonitoreo;

    /**
     * @param jwtService      valida el token JWT del administrador
     * @param resolverErrores convierte las excepciones en la respuesta JSON de {@code GlobalExceptionHandler}
     * @param tokenMonitoreo  token fijo de solo lectura para Prometheus; vacío lo desactiva
     */
    public MonitoreoSeguridadFilter(JwtService jwtService,
                                    @Qualifier("handlerExceptionResolver") HandlerExceptionResolver resolverErrores,
                                    @Value("${tecnicoya.monitoreo.token:}") String tokenMonitoreo) {
        this.jwtService = jwtService;
        this.resolverErrores = resolverErrores;
        this.tokenMonitoreo = tokenMonitoreo.trim().getBytes(StandardCharsets.UTF_8);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String ruta = request.getRequestURI();
        return !(ruta.equals(PREFIJO_ACTUATOR) || ruta.startsWith(PREFIJO_ACTUATOR + "/"));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        boolean salud = "GET".equals(request.getMethod())
                && (request.getRequestURI().equals(RUTA_SALUD) || request.getRequestURI().startsWith(RUTA_SALUD + "/"));
        String token = leerToken(request);

        if (token != null && esTokenMonitoreo(token)) {
            if (!"GET".equals(request.getMethod())) {
                rechazar(request, response, new AccesoDenegadoException(
                        "El token de monitoreo solo permite consultar (GET)"), USUARIO_PROMETHEUS);
                return;
            }
            chain.doFilter(conUsuario(request, USUARIO_PROMETHEUS, null), response);
            return;
        }

        Claims datos = token == null ? null : validar(token);
        TipoUsuario rol = datos == null ? null : TipoUsuario.valueOf(datos.get("rol", String.class));
        if (rol == TipoUsuario.ADMINISTRADOR) {
            MDC.put(MDC_USUARIO, datos.getSubject() + ":" + rol);
            try {
                chain.doFilter(conUsuario(request, datos.getSubject(), rol.name()), response);
            } finally {
                MDC.remove(MDC_USUARIO);
            }
        } else if (salud) {
            // Público: sin usuario, Actuator solo muestra el estado general (UP o DOWN)
            chain.doFilter(request, response);
        } else if (rol != null) {
            rechazar(request, response, new AccesoDenegadoException(
                    "Su rol (" + rol + ") no tiene permiso para ver el monitoreo"), datos.getSubject());
        } else {
            rechazar(request, response, new NoAutenticadoException(
                    "El monitoreo es solo para administradores: envíe su token en 'Authorization: Bearer <token>'"),
                    null);
        }
    }

    private static String leerToken(HttpServletRequest request) {
        String cabecera = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (cabecera == null || !cabecera.startsWith(PREFIJO_TOKEN)) {
            return null;
        }
        return cabecera.substring(PREFIJO_TOKEN.length()).trim();
    }

    private boolean esTokenMonitoreo(String token) {
        // MessageDigest.isEqual compara en tiempo constante: no revela cuántos caracteres coinciden
        return tokenMonitoreo.length > 0
                && MessageDigest.isEqual(tokenMonitoreo, token.getBytes(StandardCharsets.UTF_8));
    }

    /** @return los datos del token JWT, o {@code null} si no es válido */
    private Claims validar(String token) {
        try {
            return jwtService.validarToken(token);
        } catch (JwtException | IllegalArgumentException ex) {
            return null;
        }
    }

    private void rechazar(HttpServletRequest request, HttpServletResponse response, RuntimeException error,
                          String usuario) {
        Auditoria.LOG.warn("MONITOREO_DENEGADO usuario={} {} {} ip={}", Auditoria.limpiar(usuario),
                Auditoria.limpiar(request.getMethod()), Auditoria.limpiar(request.getRequestURI()),
                Auditoria.limpiar(request.getRemoteAddr()));
        resolverErrores.resolveException(request, response, null, error);
    }

    /**
     * Presenta la petición como hecha por un usuario con ese rol: Actuator lo consulta para decidir si muestra el
     * detalle de {@code /actuator/health} ({@code management.endpoint.health.roles}).
     */
    private static HttpServletRequest conUsuario(HttpServletRequest request, String nombre, String rol) {
        Principal usuario = () -> nombre;
        return new HttpServletRequestWrapper(request) {
            @Override
            public Principal getUserPrincipal() {
                return usuario;
            }

            @Override
            public boolean isUserInRole(String rolConsultado) {
                return rol != null && rol.equals(rolConsultado);
            }
        };
    }
}
