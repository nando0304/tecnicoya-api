package com.tecnicoya.api.controller;

import com.tecnicoya.api.dto.request.SuscripcionRequest;
import com.tecnicoya.api.dto.response.SuscripcionResponse;
import com.tecnicoya.api.service.SuscripcionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

/** CRUD de suscripciones de técnicos a planes. */
@RestController
@RequestMapping("/api/suscripciones")
@RequiredArgsConstructor
public class SuscripcionController {

    private final SuscripcionService suscripcionService;

    @GetMapping
    public List<SuscripcionResponse> listar() {
        return suscripcionService.listar();
    }

    @GetMapping("/{id}")
    public SuscripcionResponse obtenerPorId(@PathVariable Long id) {
        return suscripcionService.obtenerPorId(id);
    }

    @PostMapping
    public ResponseEntity<SuscripcionResponse> crear(@Valid @RequestBody SuscripcionRequest request) {
        SuscripcionResponse creado = suscripcionService.crear(request);
        URI ubicacion = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(creado.idSuscripcion()).toUri();
        return ResponseEntity.created(ubicacion).body(creado);
    }

    @PutMapping("/{id}")
    public SuscripcionResponse actualizar(@PathVariable Long id, @Valid @RequestBody SuscripcionRequest request) {
        return suscripcionService.actualizar(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id) {
        suscripcionService.eliminar(id);
    }
}
