package com.tecnicoya.api.monitoreo;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;
import java.util.regex.Pattern;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class IdPeticionFilter extends OncePerRequestFilter {
    public static final String CABECERA = "X-Request-Id";

    public static final String MDC_ID_PETICION = "idPeticion";

    private static final Pattern FORMATO_SEGURO = Pattern.compile("[A-Za-z0-9-]{8,64}");

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String idPeticion = idValido(request.getHeader(CABECERA));
        MDC.put(MDC_ID_PETICION, idPeticion);
        response.setHeader(CABECERA, idPeticion);
        try {
            chain.doFilter(request, response);
        } finally {
            MDC.remove(MDC_ID_PETICION);
        }
    }

    static String idValido(String recibido) {
        if (recibido != null && FORMATO_SEGURO.matcher(recibido).matches()) {
            return recibido;
        }
        return UUID.randomUUID().toString();
    }
}
