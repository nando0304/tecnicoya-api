package com.tecnicoya.api.config;

import com.tecnicoya.api.controller.MonitoreoController;
import com.tecnicoya.api.security.AuditoriaInterceptor;
import com.tecnicoya.api.security.JwtInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

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
