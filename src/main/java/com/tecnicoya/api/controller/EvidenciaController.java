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

/** CRUD de evidencias (documentos) de los técnicos. */
@RestController
@RequestMapping("/api/evidencias")
@RequiredArgsConstructor
public class EvidenciaController {

    private final EvidenciaService evidenciaService;

    @GetMapping
    public List<EvidenciaResponse> listar() {
        return evidenciaService.listar();
    }

    @GetMapping("/{id}")
    public EvidenciaResponse obtenerPorId(@PathVariable Long id) {
        return evidenciaService.obtenerPorId(id);
    }

    @PostMapping
    public ResponseEntity<EvidenciaResponse> crear(@Valid @RequestBody EvidenciaRequest request) {
        EvidenciaResponse creado = evidenciaService.crear(request);
        URI ubicacion = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(creado.idEvidencia()).toUri();
        return ResponseEntity.created(ubicacion).body(creado);
    }

    @PutMapping("/{id}")
    public EvidenciaResponse actualizar(@PathVariable Long id, @Valid @RequestBody EvidenciaRequest request) {
        return evidenciaService.actualizar(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id) {
        evidenciaService.eliminar(id);
    }
}
