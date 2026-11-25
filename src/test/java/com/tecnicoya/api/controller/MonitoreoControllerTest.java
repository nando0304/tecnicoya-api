package com.tecnicoya.api.controller;

import com.tecnicoya.api.exception.GlobalExceptionHandler;
import com.tecnicoya.api.monitoreo.RegistroErroresFrontend;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.util.unit.DataSize;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class MonitoreoControllerTest {
    private static final String ERROR_VALIDO = """
            {"mensaje": "Cannot read properties of undefined (reading 'nombre')",
             "pila": "TypeError: Cannot read properties of undefined\\n    at PerfilTecnico (perfil.tsx:42)",
             "url": "http://localhost:5173/tecnicos/7", "navegador": "Mozilla/5.0", "tipo": "error"}
            """;

    private final MeterRegistry registry = new SimpleMeterRegistry();
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new MonitoreoController(new RegistroErroresFrontend(registry)))
                .setControllerAdvice(new GlobalExceptionHandler(DataSize.ofMegabytes(5)))
                .build();
    }

    @Test
    void errorValido_responde202YSumaLaMetrica() throws Exception {
        reportar(ERROR_VALIDO).andExpect(status().isAccepted());

        assertThat(registry.get("tecnicoya.errores.frontend").counter().count()).isEqualTo(1.0);
    }

    @Test
    void sinMensaje_responde400() throws Exception {
        reportar("{\"url\": \"http://localhost:5173/\"}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.mensaje").value("El mensaje del error es obligatorio"));
    }

    @Test
    void demasiadosReportesDeLaMismaIp_responde429() throws Exception {
        for (int i = 0; i < 30; i++) {
            reportar(ERROR_VALIDO).andExpect(status().isAccepted());
        }

        reportar(ERROR_VALIDO).andExpect(status().isTooManyRequests());
        assertThat(registry.get("tecnicoya.errores.frontend").counter().count()).isEqualTo(30.0);
    }

    private ResultActions reportar(String json) throws Exception {
        return mockMvc.perform(post(MonitoreoController.RUTA_ERRORES_FRONTEND)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json));
    }
}
