package com.tecnicoya.api.config;

import com.tecnicoya.api.controller.MonitoreoController;
import com.tecnicoya.api.security.AuditoriaInterceptor;
import com.tecnicoya.api.security.JwtInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Registra los interceptores de seguridad en todas las rutas de la API, en este orden:
 * <ol>
 *   <li>{@link JwtInterceptor}: exige el token y verifica el rol.</li>
 *   <li>{@link AuditoriaInterceptor}: registra las operaciones que modifican datos.</li>
 * </ol>
 * La ruta pública de reporte de errores del front-end ({@link MonitoreoController#RUTA_ERRORES_FRONTEND}) queda
 * fuera de ambos: no pide token y no se audita como operación. Los endpoints de {@code /actuator} los protege
 * {@link com.tecnicoya.api.security.MonitoreoSeguridadFilter}.
 */
@Configuration
@RequiredArgsConstructor
public class JwtConfig implements WebMvcConfigurer {

    private final JwtInterceptor jwtInterceptor;
    private final AuditoriaInterceptor auditoriaInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(jwtInterceptor).addPathPatterns("/api/**")
                .excludePathPatterns(MonitoreoController.RUTA_ERRORES_FRONTEND).order(1);
        registry.addInterceptor(auditoriaInterceptor).addPathPatterns("/api/**")
                .excludePathPatterns(MonitoreoController.RUTA_ERRORES_FRONTEND).order(2);
    }
}
