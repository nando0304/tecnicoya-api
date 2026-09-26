package com.tecnicoya.api.controller;

import com.tecnicoya.api.dto.request.CalificacionRequest;
import com.tecnicoya.api.dto.response.CalificacionResponse;
import com.tecnicoya.api.service.CalificacionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

/** CRUD de calificaciones de servicios. */
@RestController
@RequestMapping("/api/calificaciones")
@RequiredArgsConstructor
public class CalificacionController {

    private final CalificacionService calificacionService;

    @GetMapping
    public List<CalificacionResponse> listar() {
        return calificacionService.listar();
    }

    @GetMapping("/{id}")
    public CalificacionResponse obtenerPorId(@PathVariable Long id) {
        return calificacionService.obtenerPorId(id);
    }

    @PostMapping
    public ResponseEntity<CalificacionResponse> crear(@Valid @RequestBody CalificacionRequest request) {
        CalificacionResponse creado = calificacionService.crear(request);
        URI ubicacion = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(creado.idCalificacion()).toUri();
        return ResponseEntity.created(ubicacion).body(creado);
    }

    @PutMapping("/{id}")
    public CalificacionResponse actualizar(@PathVariable Long id, @Valid @RequestBody CalificacionRequest request) {
        return calificacionService.actualizar(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id) {
        calificacionService.eliminar(id);
    }
}
