package com.tecnicoya.api.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Agrega a todas las respuestas cabeceras HTTP de seguridad recomendadas por OWASP:
 *
 * <ul>
 *   <li>{@code X-Content-Type-Options: nosniff}: el navegador no adivina el tipo de contenido (evita que un archivo
 *       subido se ejecute como script).</li>
 *   <li>{@code X-Frame-Options: DENY}: la API no puede mostrarse dentro de un {@code <iframe>} (clickjacking).</li>
 *   <li>{@code Referrer-Policy: no-referrer}: no filtra la URL de origen a otros sitios.</li>
 *   <li>{@code Content-Security-Policy} y {@code Cache-Control: no-store} en las respuestas JSON: si alguien abre
 *       una respuesta en el navegador no se ejecuta ningún script (XSS) y los datos personales no quedan en
 *       caché. No se aplican a {@code /api/archivos}, que el navegador muestra como imágenes o PDF.</li>
 *   <li>{@code Strict-Transport-Security}: solo cuando la petición llega por HTTPS, obliga a seguir usándolo.</li>
 * </ul>
 */
@Component
public class CabecerasSeguridadFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        response.setHeader("X-Content-Type-Options", "nosniff");
        response.setHeader("X-Frame-Options", "DENY");
        response.setHeader("Referrer-Policy", "no-referrer");
        if (!request.getRequestURI().startsWith("/api/archivos/")) {
            response.setHeader("Content-Security-Policy", "default-src 'none'; frame-ancestors 'none'");
            response.setHeader(HttpHeaders.CACHE_CONTROL, "no-store");
        }
        if (request.isSecure()) {
            response.setHeader("Strict-Transport-Security", "max-age=31536000; includeSubDomains");
        }
        chain.doFilter(request, response);
    }
}
