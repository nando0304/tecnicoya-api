package com.tecnicoya.api.controller;

import com.tecnicoya.api.dto.request.ErrorFrontendRequest;
import com.tecnicoya.api.monitoreo.RegistroErroresFrontend;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Rastreo de errores del front-end: el navegador reporta aquí los errores de JavaScript que le ocurren al usuario.
 *
 * <p>Es una ruta pública (un error puede ocurrir antes de iniciar sesión), así que {@code JwtConfig} la excluye del
 * token y de la auditoría; {@link RegistroErroresFrontend} limita los reportes por IP. Las métricas y la salud de la
 * API están en {@code /actuator} (ver {@code docs/monitoreo.md}).
 */
@RestController
@RequiredArgsConstructor
public class MonitoreoController {

    /** Ruta pública que recibe los errores del front-end. */
    public static final String RUTA_ERRORES_FRONTEND = "/api/monitoreo/errores-frontend";

    private final RegistroErroresFrontend registroErrores;

    /**
     * {@code POST /api/monitoreo/errores-frontend}: registra un error de JavaScript del navegador.
     *
     * @param error datos del error
     * @param http  petición HTTP, para obtener la IP del cliente
     */
    @PostMapping(RUTA_ERRORES_FRONTEND)
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void reportarErrorFrontend(@Valid @RequestBody ErrorFrontendRequest error, HttpServletRequest http) {
        registroErrores.registrar(error, http.getRemoteAddr());
    }
}
