package com.tecnicoya.api.security;

import com.google.common.base.Stopwatch;
import com.google.common.collect.ImmutableSet;
import com.tecnicoya.api.util.Auditoria;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * Registra en el log de auditoría cada operación que modifica datos ({@code POST}, {@code PUT}, {@code PATCH},
 * {@code DELETE}): quién la hizo, sobre qué ruta, con qué resultado y cuánto demoró.
 *
 * <p>Se ejecuta después de {@link JwtInterceptor}, por lo que el usuario ya está identificado. Mide el tiempo con
 * {@link Stopwatch} de Google Guava.
 */
@Component
public class AuditoriaInterceptor implements HandlerInterceptor {

    private static final ImmutableSet<String> METODOS_AUDITADOS = ImmutableSet.of("POST", "PUT", "PATCH", "DELETE");
    private static final String ATRIBUTO_CRONOMETRO = AuditoriaInterceptor.class.getName() + ".cronometro";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (METODOS_AUDITADOS.contains(request.getMethod())) {
            request.setAttribute(ATRIBUTO_CRONOMETRO, Stopwatch.createStarted());
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler,
                                Exception ex) {
        if (!(request.getAttribute(ATRIBUTO_CRONOMETRO) instanceof Stopwatch cronometro)) {
            return;
        }
        Object usuario = Objects.requireNonNullElse(request.getAttribute(JwtInterceptor.ATRIBUTO_USUARIO), "anónimo");
        Object rol = Objects.requireNonNullElse(request.getAttribute(JwtInterceptor.ATRIBUTO_ROL), "-");
        Auditoria.LOG.info("OPERACION {} {} -> {} usuario={} rol={} ({} ms)", Auditoria.limpiar(request.getMethod()),
                Auditoria.limpiar(request.getRequestURI()), response.getStatus(), Auditoria.limpiar(usuario), Auditoria.limpiar(rol),
                cronometro.elapsed(TimeUnit.MILLISECONDS));
    }
}
