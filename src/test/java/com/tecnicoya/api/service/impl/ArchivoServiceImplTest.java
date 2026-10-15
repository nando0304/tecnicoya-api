package com.tecnicoya.api.service.impl;

import com.tecnicoya.api.exception.RecursoNoEncontradoException;
import com.tecnicoya.api.exception.ReglaNegocioException;
import com.tecnicoya.api.service.ArchivoAlmacenado;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.core.io.InputStreamSource;
import org.springframework.core.io.Resource;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ArchivoServiceImplTest {

    /** SHA-256 de "abc", vector de prueba del estándar FIPS 180-2. */
    private static final String SHA256_ABC = "ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad";

    @TempDir
    private Path directorio;
    private ArchivoServiceImpl archivoService;

    @BeforeEach
    void setUp() {
        archivoService = new ArchivoServiceImpl(directorio.toString());
    }

    @Test
    void guardar_usaLaHuellaSha256ComoNombre() {
        ArchivoAlmacenado guardado = archivoService.guardar("Certificado.PDF", contenido("abc"));

        assertThat(guardado.nombre()).isEqualTo(SHA256_ABC + ".pdf");
        assertThat(guardado.sha256()).isEqualTo(SHA256_ABC);
        assertThat(guardado.tamanoBytes()).isEqualTo(3);
        assertThat(guardado.nombreOriginal()).isEqualTo("Certificado.PDF");
        assertThat(directorio.resolve(guardado.nombre())).hasContent("abc");
    }

    @Test
    void guardar_mismoContenidoDosVeces_loGuardaUnaSolaVez() throws IOException {
        archivoService.guardar("foto.png", contenido("abc"));
        archivoService.guardar("otra-foto.png", contenido("abc"));

        try (Stream<Path> archivos = Files.list(directorio)) {
            assertThat(archivos).hasSize(1);
        }
    }

    @Test
    void guardar_descartaLaRutaDelNombreOriginal() {
        assertThat(archivoService.guardar("C:\\fakepath\\cv.pdf", contenido("abc")).nombreOriginal()).isEqualTo("cv.pdf");
        assertThat(archivoService.guardar("../../cv.pdf", contenido("abc")).nombreOriginal()).isEqualTo("cv.pdf");
    }

    @Test
    void guardar_tipoNoPermitido_lanzaReglaNegocio() throws IOException {
        assertThatThrownBy(() -> archivoService.guardar("pagina.html", contenido("<script>")))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("pdf, jpg, jpeg, png, webp");
        assertThatThrownBy(() -> archivoService.guardar("sin-extension", contenido("abc")))
                .isInstanceOf(ReglaNegocioException.class);
        assertThatThrownBy(() -> archivoService.guardar(null, contenido("abc")))
                .isInstanceOf(ReglaNegocioException.class);
        assertThat(directorio).isEmptyDirectory();
    }

    @Test
    void guardar_nombreConByteNulo_lanzaReglaNegocio() {
        assertThatThrownBy(() -> archivoService.guardar("cv.php\u0000.pdf", contenido("abc")))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessage("El nombre del archivo no es válido");
    }

    @Test
    void guardar_archivoVacio_lanzaReglaNegocio() {
        assertThatThrownBy(() -> archivoService.guardar("vacio.pdf", contenido("")))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessage("El archivo está vacío");
        assertThat(directorio).isEmptyDirectory();
    }

    @Test
    void cargar_devuelveElArchivoGuardado() throws IOException {
        String nombre = archivoService.guardar("cv.pdf", contenido("abc")).nombre();

        Resource archivo = archivoService.cargar(nombre);

        assertThat(archivo.getContentAsString(StandardCharsets.UTF_8)).isEqualTo("abc");
    }

    @Test
    void cargar_nombreFueraDelFormato_lanzaNoEncontrado() throws IOException {
        Files.writeString(directorio.resolve("secreto.pdf"), "no debe poder leerse");

        assertThatThrownBy(() -> archivoService.cargar("secreto.pdf"))
                .isInstanceOf(RecursoNoEncontradoException.class);
        assertThatThrownBy(() -> archivoService.cargar("../application.properties"))
                .isInstanceOf(RecursoNoEncontradoException.class);
    }

    @Test
    void cargar_inexistente_lanzaNoEncontrado() {
        assertThatThrownBy(() -> archivoService.cargar(SHA256_ABC + ".pdf"))
                .isInstanceOf(RecursoNoEncontradoException.class)
                .hasMessage("No se encontró el archivo " + SHA256_ABC + ".pdf");
    }

    private static InputStreamSource contenido(String texto) {
        return () -> new ByteArrayInputStream(texto.getBytes(StandardCharsets.UTF_8));
    }
}
