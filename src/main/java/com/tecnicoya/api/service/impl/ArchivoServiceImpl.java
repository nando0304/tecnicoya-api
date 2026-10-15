package com.tecnicoya.api.service.impl;

import com.tecnicoya.api.exception.RecursoNoEncontradoException;
import com.tecnicoya.api.exception.ReglaNegocioException;
import com.tecnicoya.api.service.ArchivoAlmacenado;
import com.tecnicoya.api.service.ArchivoService;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.input.BoundedInputStream;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.InputStreamSource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

/** Implementación de {@link ArchivoService} que guarda los archivos en una carpeta del servidor. */
@Service
public class ArchivoServiceImpl implements ArchivoService {

    private static final List<String> EXTENSIONES_PERMITIDAS = List.of("pdf", "jpg", "jpeg", "png", "webp");
    /** Nombre de un archivo guardado: 64 dígitos hexadecimales (SHA-256) y una extensión permitida. */
    private static final Pattern NOMBRE_ALMACENADO =
            Pattern.compile("[0-9a-f]{64}\\.(?:" + String.join("|", EXTENSIONES_PERMITIDAS) + ")");

    private final Path directorio;

    /**
     * @param directorio carpeta donde se guardan los archivos; se crea si no existe
     */
    public ArchivoServiceImpl(@Value("${tecnicoya.archivos.directorio}") String directorio) {
        this.directorio = Path.of(directorio).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.directorio);
        } catch (IOException ex) {
            throw new UncheckedIOException("No se pudo crear la carpeta de archivos " + this.directorio, ex);
        }
    }

    @Override
    public ArchivoAlmacenado guardar(String nombreOriginal, InputStreamSource contenido) {
        String nombre;
        String extension;
        try {
            nombre = FilenameUtils.getName(nombreOriginal);
            extension = StringUtils.lowerCase(FilenameUtils.getExtension(nombre), Locale.ROOT);
        } catch (IllegalArgumentException ex) {
            // FilenameUtils rechaza nombres con bytes nulos y, en Windows, con ':' (flujos alternos de NTFS)
            throw new ReglaNegocioException("El nombre del archivo no es válido");
        }
        if (extension == null || !EXTENSIONES_PERMITIDAS.contains(extension)) {
            throw new ReglaNegocioException("Tipo de archivo no permitido; los formatos aceptados son: "
                    + String.join(", ", EXTENSIONES_PERMITIDAS));
        }

        try {
            String sha256;
            long tamano;
            // Primera lectura: huella y tamaño, sin cargar el archivo en memoria
            try (BoundedInputStream entrada = BoundedInputStream.builder()
                    .setInputStream(contenido.getInputStream()).get()) {
                sha256 = DigestUtils.sha256Hex(entrada);
                tamano = entrada.getCount();
            }
            if (tamano == 0) {
                throw new ReglaNegocioException("El archivo está vacío");
            }

            String nombreAlmacenado = sha256 + "." + extension;
            Path destino = directorio.resolve(nombreAlmacenado);
            if (Files.notExists(destino)) {
                escribir(contenido, destino);
            }
            return new ArchivoAlmacenado(nombreAlmacenado, nombre, tamano, sha256);
        } catch (IOException ex) {
            throw new UncheckedIOException("No se pudo guardar el archivo " + nombre, ex);
        }
    }

    @Override
    public Resource cargar(String nombre) {
        // El patrón se valida antes de construir la ruta, así no se puede salir de la carpeta con "../"
        if (nombre == null || !NOMBRE_ALMACENADO.matcher(nombre).matches()
                || !Files.isRegularFile(directorio.resolve(nombre))) {
            throw new RecursoNoEncontradoException("No se encontró el archivo " + nombre);
        }
        return new FileSystemResource(directorio.resolve(nombre));
    }

    /** Escribe primero en un temporal y luego lo renombra, para que nunca se sirva un archivo a medio escribir. */
    private void escribir(InputStreamSource contenido, Path destino) throws IOException {
        Path temporal = Files.createTempFile(directorio, "subida-", ".tmp");
        try (InputStream entrada = contenido.getInputStream()) {
            Files.copy(entrada, temporal, StandardCopyOption.REPLACE_EXISTING);
            Files.move(temporal, destino, StandardCopyOption.REPLACE_EXISTING);
        } finally {
            Files.deleteIfExists(temporal);
        }
    }
}
