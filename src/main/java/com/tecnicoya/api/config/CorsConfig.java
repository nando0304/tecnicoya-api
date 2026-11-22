package com.tecnicoya.api.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * CORS restringido a los orígenes del front-end definidos en {@code tecnicoya.cors.origenes} (por defecto, el
 * servidor de desarrollo de Vite). Así, otra página web abierta en el navegador del usuario no puede llamar a la API
 * con su sesión. Postman y otras herramientas que no son navegadores no se ven afectadas por CORS.
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    private final String[] origenes;

    /**
     * @param origenes orígenes permitidos separados por comas, p. ej. {@code http://localhost:5173}
     */
    public CorsConfig(@Value("${tecnicoya.cors.origenes}") String[] origenes) {
        this.origenes = origenes.clone();
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOriginPatterns(origenes)
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                // X-Request-Id: id de la petición (IdPeticionFilter), para que el front-end lo muestre si algo falla
                .exposedHeaders("Location", "Content-Disposition", "X-Request-Id")
                .maxAge(3600);
    }
}
