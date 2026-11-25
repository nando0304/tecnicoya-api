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
        return tokenMonitoreo.length > 0
                && MessageDigest.isEqual(tokenMonitoreo, token.getBytes(StandardCharsets.UTF_8));
    }

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
