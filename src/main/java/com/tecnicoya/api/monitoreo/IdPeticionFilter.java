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

/**
 * Da a cada petición un identificador único (id de correlación) para rastrear errores de punta a punta.
 *
 * <p>El id se guarda en el MDC de Logback con la clave {@link #MDC_ID_PETICION}: aparece como campo
 * {@code idPeticion} en cada evento del log JSON ({@code logs/tecnicoya-api.json}) escrito durante esa petición.
 * También se devuelve al cliente en la cabecera {@link #CABECERA}; si el front-end muestra ese código al usuario
 * cuando algo falla, basta buscarlo en el log para ver qué pasó, con su traza completa.
 *
 * <p>Si el cliente ya envía un {@code X-Request-Id} con formato seguro (letras, números y guiones, de 8 a 64
 * caracteres) se reutiliza, para seguir la misma petición entre el front-end y la API. Cualquier otro valor se
 * descarta y se genera un UUID nuevo, así nadie puede inyectar texto arbitrario en los logs ni en las cabeceras.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class IdPeticionFilter extends OncePerRequestFilter {

    /** Cabecera HTTP con el id de la petición, en la solicitud (opcional) y en la respuesta. */
    public static final String CABECERA = "X-Request-Id";
    /** Clave del MDC de Logback con el id de la petición. */
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

    /**
     * @param recibido valor de la cabecera {@code X-Request-Id} enviada por el cliente (puede ser {@code null})
     * @return el mismo valor si tiene un formato seguro; si no, un UUID nuevo
     */
    static String idValido(String recibido) {
        if (recibido != null && FORMATO_SEGURO.matcher(recibido).matches()) {
            return recibido;
        }
        return UUID.randomUUID().toString();
    }
}
