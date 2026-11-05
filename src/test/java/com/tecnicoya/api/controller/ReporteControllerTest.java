package com.tecnicoya.api.controller;

import com.tecnicoya.api.dto.response.UsuarioResponse;
import com.tecnicoya.api.exception.GlobalExceptionHandler;
import com.tecnicoya.api.model.enums.EstadoUsuario;
import com.tecnicoya.api.model.enums.TipoUsuario;
import com.tecnicoya.api.security.CabecerasSeguridadFilter;
import com.tecnicoya.api.security.JwtInterceptor;
import com.tecnicoya.api.security.JwtService;
import com.tecnicoya.api.service.ReporteService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.util.unit.DataSize;

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Los reportes solo los descarga un administrador (principio de mínimo privilegio, {@code @RequiereRol}). */
class ReporteControllerTest {

    private final JwtService jwtService = new JwtService("clave-de-prueba-con-mas-de-32-caracteres", 8);
    private final ReporteService reporteService = mock(ReporteService.class);
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new ReporteController(reporteService))
                .addMappedInterceptors(new String[]{"/api/**"}, new JwtInterceptor(jwtService))
                .addFilters(new CabecerasSeguridadFilter())
                .setControllerAdvice(new GlobalExceptionHandler(DataSize.ofMegabytes(5)))
                .build();
    }

    @Test
    void administrador_descargaElExcel() throws Exception {
        when(reporteService.reporteUsuarios()).thenReturn(new byte[]{1, 2, 3});

        mockMvc.perform(get("/api/reportes/usuarios").header(HttpHeaders.AUTHORIZATION, bearer(TipoUsuario.ADMINISTRADOR)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(ReporteService.TIPO_XLSX))
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, containsString("tecnicoya-usuarios-")))
                .andExpect(content().bytes(new byte[]{1, 2, 3}));
    }

    @Test
    void cliente_recibe403() throws Exception {
        mockMvc.perform(get("/api/reportes/general").header(HttpHeaders.AUTHORIZATION, bearer(TipoUsuario.CLIENTE)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.mensaje").value(containsString("CLIENTE")));
        verify(reporteService, never()).reporteGeneral();
    }

    @Test
    void sinToken_recibe401_conCabecerasDeSeguridad() throws Exception {
        mockMvc.perform(get("/api/reportes/servicios"))
                .andExpect(status().isUnauthorized())
                .andExpect(header().string("X-Content-Type-Options", "nosniff"))
                .andExpect(header().string("X-Frame-Options", "DENY"))
                .andExpect(header().string("Content-Security-Policy", containsString("default-src 'none'")));
    }

    private String bearer(TipoUsuario rol) {
        UsuarioResponse usuario = new UsuarioResponse(7L, "Ana", "Torres", "ana@correo.example", null, rol,
                EstadoUsuario.ACTIVO, LocalDateTime.now());
        return "Bearer " + jwtService.generarToken(usuario);
    }
}
