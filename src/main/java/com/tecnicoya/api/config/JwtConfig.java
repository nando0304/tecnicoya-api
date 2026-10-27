package com.tecnicoya.api.config;

import com.tecnicoya.api.security.JwtInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/** Registra el {@link JwtInterceptor} para que revise el token en todas las rutas de la API. */
@Configuration
@RequiredArgsConstructor
public class JwtConfig implements WebMvcConfigurer {

    private final JwtInterceptor jwtInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(jwtInterceptor).addPathPatterns("/api/**");
    }
}
