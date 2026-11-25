package com.tecnicoya.api.controller;

import com.tecnicoya.api.model.enums.TipoUsuario;
import com.tecnicoya.api.security.JwtInterceptor;
import com.tecnicoya.api.security.RequiereRol;
import com.tecnicoya.api.service.ReporteService;
import com.tecnicoya.api.util.Auditoria;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.function.Supplier;

@RestController
@RequestMapping("/api/reportes")
@RequiredArgsConstructor
@RequiereRol(TipoUsuario.ADMINISTRADOR)
public class ReporteController {
    private static final MediaType XLSX = MediaType.parseMediaType(ReporteService.TIPO_XLSX);

    private final ReporteService reporteService;

    @GetMapping("/usuarios")
    public ResponseEntity<byte[]> usuarios(HttpServletRequest request) {
        return descargar("usuarios", reporteService::reporteUsuarios, request);
    }

    @GetMapping("/tecnicos")
    public ResponseEntity<byte[]> tecnicos(HttpServletRequest request) {
        return descargar("tecnicos", reporteService::reporteTecnicos, request);
    }

    @GetMapping("/servicios")
    public ResponseEntity<byte[]> servicios(HttpServletRequest request) {
        return descargar("servicios", reporteService::reporteServicios, request);
    }

    @GetMapping("/general")
    public ResponseEntity<byte[]> general(HttpServletRequest request) {
        return descargar("general", reporteService::reporteGeneral, request);
    }

    private static ResponseEntity<byte[]> descargar(String nombre, Supplier<byte[]> generador,
                                                    HttpServletRequest request) {
        byte[] contenido = generador.get();
        String archivo = "tecnicoya-" + nombre + "-" + LocalDate.now() + ".xlsx";
        Auditoria.LOG.info("REPORTE_DESCARGADO {} usuario={}", archivo,
                Auditoria.limpiar(request.getAttribute(JwtInterceptor.ATRIBUTO_USUARIO)));
        return ResponseEntity.ok()
                .contentType(XLSX)
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment().filename(archivo).build()
                        .toString())
                .body(contenido);
    }
}
