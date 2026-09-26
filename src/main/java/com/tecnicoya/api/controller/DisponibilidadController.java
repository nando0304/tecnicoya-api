package com.tecnicoya.api.controller;

import com.tecnicoya.api.dto.request.DisponibilidadRequest;
import com.tecnicoya.api.dto.response.DisponibilidadResponse;
import com.tecnicoya.api.service.DisponibilidadService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

/** CRUD de franjas de disponibilidad de los técnicos. */
@RestController
@RequestMapping("/api/disponibilidades")
@RequiredArgsConstructor
public class DisponibilidadController {

    private final DisponibilidadService disponibilidadService;

    @GetMapping
    public List<DisponibilidadResponse> listar() {
        return disponibilidadService.listar();
    }

    @GetMapping("/{id}")
    public DisponibilidadResponse obtenerPorId(@PathVariable Long id) {
        return disponibilidadService.obtenerPorId(id);
    }

    @PostMapping
    public ResponseEntity<DisponibilidadResponse> crear(@Valid @RequestBody DisponibilidadRequest request) {
        DisponibilidadResponse creado = disponibilidadService.crear(request);
        URI ubicacion = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(creado.idDisponibilidad()).toUri();
        return ResponseEntity.created(ubicacion).body(creado);
    }

    @PutMapping("/{id}")
    public DisponibilidadResponse actualizar(@PathVariable Long id, @Valid @RequestBody DisponibilidadRequest request) {
        return disponibilidadService.actualizar(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id) {
        disponibilidadService.eliminar(id);
    }
}
