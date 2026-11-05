package com.tecnicoya.api.controller;

import com.tecnicoya.api.dto.request.LoginRequest;
import com.tecnicoya.api.exception.CredencialesInvalidasException;
import com.tecnicoya.api.exception.GlobalExceptionHandler;
import com.tecnicoya.api.security.JwtService;
import com.tecnicoya.api.security.LimiteIntentosLogin;
import com.tecnicoya.api.service.UsuarioService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.util.unit.DataSize;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Protección contra fuerza bruta en el login: tras 3 fallos el cuarto intento recibe 429. */
class AuthControllerTest {

    private final UsuarioService usuarioService = mock(UsuarioService.class);
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        AuthController controller = new AuthController(usuarioService, new JwtService("clave-de-prueba-con-mas-de-32-caracteres", 8),
                new LimiteIntentosLogin(3, 15));
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler(DataSize.ofMegabytes(5)))
                .build();
        when(usuarioService.autenticar(any(LoginRequest.class)))
                .thenThrow(new CredencialesInvalidasException("Correo o contraseña incorrectos"));
    }

    @Test
    void trasVariosFallos_bloqueaConCodigo429() throws Exception {
        for (int i = 0; i < 3; i++) {
            login().andExpect(status().isUnauthorized());
        }

        login().andExpect(status().isTooManyRequests())
                .andExpect(header().string(HttpHeaders.RETRY_AFTER, "900"))
                .andExpect(jsonPath("$.error").value("Demasiadas solicitudes"));
        // El intento bloqueado ni siquiera llega a consultar la base de datos
        verify(usuarioService, times(3)).autenticar(any(LoginRequest.class));
    }

    private ResultActions login() throws Exception {
        return mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"correo\":\"maria@correo.example\",\"contrasena\":\"incorrecta1\"}"));
    }
}
