package com.tecnicoya.api.controller;

import com.tecnicoya.api.dto.response.ArchivoResponse;
import com.tecnicoya.api.service.ArchivoAlmacenado;
import com.tecnicoya.api.service.ArchivoService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.time.Duration;

/**
 * Subida y descarga de archivos (certificados, fotos de trabajos y comprobantes de pago), bajo {@code /api/archivos}.
 *
 * <p>El front-end sube primero el archivo y guarda la {@code url} devuelta en la evidencia o en el comprobante.
 * Las reglas se aplican en {@link ArchivoService}.
 */
@RestController
@RequestMapping("/api/archivos")
@RequiredArgsConstructor
public class ArchivoController {

    private final ArchivoService archivoService;

    /**
     * {@code POST /api/archivos}: sube un archivo enviado como {@code multipart/form-data} en el campo {@code archivo}.
     *
     * @param archivo PDF o imagen JPG, PNG o WEBP
     * @return 201 con la URL del archivo y la cabecera {@code Location};
     *         400 si falta, está vacío o su tipo no está permitido; 413 si supera el tamaño máximo
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ArchivoResponse> subir(@RequestParam("archivo") MultipartFile archivo) {
        ArchivoAlmacenado guardado = archivoService.guardar(archivo.getOriginalFilename(), archivo);
        URI url = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{nombre}").buildAndExpand(guardado.nombre()).toUri();
        return ResponseEntity.created(url).body(ArchivoResponse.desde(guardado, url));
    }

    /**
     * {@code GET /api/archivos/{nombre}}: descarga un archivo subido.
     *
     * <p>Como el nombre es la huella del contenido, el archivo nunca cambia y el navegador puede guardarlo en caché
     * sin volver a pedirlo.
     *
     * @param nombre nombre devuelto al subirlo
     * @return 200 con el archivo; 404 si no existe
     */
    @GetMapping("/{nombre}")
    public ResponseEntity<Resource> descargar(@PathVariable String nombre) {
        Resource archivo = archivoService.cargar(nombre);
        return ResponseEntity.ok()
                .contentType(MediaTypeFactory.getMediaType(archivo).orElse(MediaType.APPLICATION_OCTET_STREAM))
                .cacheControl(CacheControl.maxAge(Duration.ofDays(365)).cachePublic().immutable())
                .header("X-Content-Type-Options", "nosniff")
                .body(archivo);
    }
}
