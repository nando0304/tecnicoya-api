package com.tecnicoya.api.controller;

import com.tecnicoya.api.exception.GlobalExceptionHandler;
import com.tecnicoya.api.service.impl.ArchivoServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.util.unit.DataSize;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Prueba el controlador y el manejo de errores de punta a punta, sin base de datos ni servidor. */
class ArchivoControllerTest {

    private static final String SHA256_ABC = "ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad";

    @TempDir
    private Path directorio;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        ArchivoController controller = new ArchivoController(new ArchivoServiceImpl(directorio.toString()));
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler(DataSize.ofMegabytes(5)))
                .build();
    }

    @Test
    void subir_devuelve201ConLaUrlDelArchivo() throws Exception {
        String url = "http://localhost/api/archivos/" + SHA256_ABC + ".pdf";

        mockMvc.perform(multipart("/api/archivos").file(archivo("cv.pdf", "abc")))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", url))
                .andExpect(jsonPath("$.url").value(url))
                .andExpect(jsonPath("$.nombreOriginal").value("cv.pdf"))
                .andExpect(jsonPath("$.tamanoBytes").value(3))
                .andExpect(jsonPath("$.sha256").value(SHA256_ABC));
    }

    @Test
    void subir_tipoNoPermitido_devuelve400() throws Exception {
        mockMvc.perform(multipart("/api/archivos").file(archivo("virus.exe", "MZ")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value(containsString("Tipo de archivo no permitido")));
    }

    @Test
    void subir_sinArchivo_devuelve400IndicandoElCampo() throws Exception {
        mockMvc.perform(multipart("/api/archivos").param("otro", "valor"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("Falta el archivo; envíelo en el campo 'archivo' del formulario"));
    }

    @Test
    void subir_comoJson_devuelve415IndicandoMultipart() throws Exception {
        mockMvc.perform(post("/api/archivos").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isUnsupportedMediaType())
                .andExpect(jsonPath("$.mensaje").value(containsString("multipart/form-data")));
    }

    @Test
    void descargar_devuelveElArchivoConSuTipoYCacheInmutable() throws Exception {
        mockMvc.perform(multipart("/api/archivos").file(archivo("foto.webp", "abc")));

        mockMvc.perform(get("/api/archivos/{nombre}", SHA256_ABC + ".webp"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("image/webp"))
                .andExpect(content().string("abc"))
                .andExpect(header().string("Cache-Control", containsString("immutable")))
                .andExpect(header().string("X-Content-Type-Options", "nosniff"));
    }

    @Test
    void descargar_inexistente_devuelve404() throws Exception {
        mockMvc.perform(get("/api/archivos/{nombre}", SHA256_ABC + ".pdf"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    private static MockMultipartFile archivo(String nombre, String texto) {
        return new MockMultipartFile("archivo", nombre, MediaType.APPLICATION_OCTET_STREAM_VALUE,
                texto.getBytes(StandardCharsets.UTF_8));
    }
}
