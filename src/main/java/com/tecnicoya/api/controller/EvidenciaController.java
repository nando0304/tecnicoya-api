package com.tecnicoya.api.controller;

import com.tecnicoya.api.dto.request.EvidenciaRequest;
import com.tecnicoya.api.dto.response.EvidenciaResponse;
import com.tecnicoya.api.service.EvidenciaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

/**
 * Endpoints REST de evidencias (documentos) de los técnicos, bajo {@code /api/evidencias}.
 *
 * <p>Las reglas de negocio se aplican en {@link EvidenciaService}.
 */
@RestController
@RequestMapping("/api/evidencias")
@RequiredArgsConstructor
public class EvidenciaController {

    private final EvidenciaService evidenciaService;

    /**
     * {@code GET /api/evidencias}: lista todos los registros.
     *
     * @return 200 con la lista (vacía si no hay registros)
     */
    @GetMapping
    public List<EvidenciaResponse> listar() {
        return evidenciaService.listar();
    }

    /**
     * {@code GET /api/evidencias/{id}}: obtiene un registro.
     *
     * @param id id del registro
     * @return 200 con el registro; 404 si no existe
     */
    @GetMapping("/{id}")
    public EvidenciaResponse obtenerPorId(@PathVariable Long id) {
        return evidenciaService.obtenerPorId(id);
    }

    /**
     * {@code POST /api/evidencias}: crea un registro.
     *
     * @param request datos del nuevo registro
     * @return 201 con el registro creado y la cabecera {@code Location};
     *         400 si los datos son inválidos o incumplen una regla de negocio;
     *         404 si hace referencia a un registro inexistente; 409 si hay un conflicto
     */
    @PostMapping
    public ResponseEntity<EvidenciaResponse> crear(@Valid @RequestBody EvidenciaRequest request) {
        EvidenciaResponse creado = evidenciaService.crear(request);
        URI ubicacion = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(creado.idEvidencia()).toUri();
        return ResponseEntity.created(ubicacion).body(creado);
    }

    /**
     * {@code PUT /api/evidencias/{id}}: reemplaza los datos de un registro.
     *
     * @param id      id del registro
     * @param request nuevos datos
     * @return 200 con el registro actualizado; 400, 404 o 409 en los mismos casos que {@link #crear}
     */
    @PutMapping("/{id}")
    public EvidenciaResponse actualizar(@PathVariable Long id, @Valid @RequestBody EvidenciaRequest request) {
        return evidenciaService.actualizar(id, request);
    }

    /**
     * {@code DELETE /api/evidencias/{id}}: elimina un registro.
     * Responde 204 sin contenido, 404 si no existe o 409 si otros registros dependen de él.
     *
     * @param id id del registro
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id) {
        evidenciaService.eliminar(id);
    }
}
