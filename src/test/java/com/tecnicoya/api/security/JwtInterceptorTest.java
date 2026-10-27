package com.tecnicoya.api.security;

import com.tecnicoya.api.controller.ArchivoController;
import com.tecnicoya.api.exception.GlobalExceptionHandler;
import com.tecnicoya.api.service.impl.ArchivoServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.util.unit.DataSize;

import java.nio.file.Path;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Prueba el interceptor con un controlador real: subir un archivo pide token y descargarlo no. */
class JwtInterceptorTest {

    @TempDir
    private Path directorio;
    private MockMvc mockMvc;
    private final JwtService jwtService = new JwtService(JwtServiceTest.SECRETO, 8);

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new ArchivoController(new ArchivoServiceImpl(directorio.toString())))
                .addMappedInterceptors(new String[]{"/api/**"}, new JwtInterceptor(jwtService))
                .setControllerAdvice(new GlobalExceptionHandler(DataSize.ofMegabytes(5)))
                .build();
    }

    @Test
    void sinToken_devuelve401() throws Exception {
        mockMvc.perform(multipart("/api/archivos").file(archivo()))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.mensaje").value(containsString("Authorization: Bearer")));
    }

    @Test
    void tokenInvalido_devuelve401() throws Exception {
        mockMvc.perform(multipart("/api/archivos").file(archivo())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer token-inventado"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.mensaje").value("El token no es válido o ya venció; inicie sesión nuevamente"));
    }

    @Test
    void tokenValido_llegaAlControlador() throws Exception {
        String token = jwtService.generarToken(JwtServiceTest.USUARIO);

        mockMvc.perform(multipart("/api/archivos").file(archivo())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isCreated());
    }

    @Test
    void rutaPublica_noPideToken() throws Exception {
        // Llega al controlador sin token: responde 404 porque el archivo no existe, no 401
        mockMvc.perform(get("/api/archivos/{nombre}", "inexistente.pdf"))
                .andExpect(status().isNotFound());
    }

    private static MockMultipartFile archivo() {
        return new MockMultipartFile("archivo", "cv.pdf", MediaType.APPLICATION_PDF_VALUE, new byte[]{1, 2, 3});
    }
}
