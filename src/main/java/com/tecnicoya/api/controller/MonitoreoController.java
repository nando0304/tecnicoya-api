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

@RestController
@RequiredArgsConstructor
public class MonitoreoController {
    public static final String RUTA_ERRORES_FRONTEND = "/api/monitoreo/errores-frontend";

    private final RegistroErroresFrontend registroErrores;

    @PostMapping(RUTA_ERRORES_FRONTEND)
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void reportarErrorFrontend(@Valid @RequestBody ErrorFrontendRequest error, HttpServletRequest http) {
        registroErrores.registrar(error, http.getRemoteAddr());
    }
}
