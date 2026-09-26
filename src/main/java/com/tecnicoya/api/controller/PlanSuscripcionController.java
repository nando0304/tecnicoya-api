package com.tecnicoya.api.controller;

import com.tecnicoya.api.dto.request.PlanSuscripcionRequest;
import com.tecnicoya.api.dto.response.PlanSuscripcionResponse;
import com.tecnicoya.api.service.PlanSuscripcionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

/** CRUD de planes de suscripción. */
@RestController
@RequestMapping("/api/planes-suscripcion")
@RequiredArgsConstructor
public class PlanSuscripcionController {

    private final PlanSuscripcionService planSuscripcionService;

    @GetMapping
    public List<PlanSuscripcionResponse> listar() {
        return planSuscripcionService.listar();
    }

    @GetMapping("/{id}")
    public PlanSuscripcionResponse obtenerPorId(@PathVariable Long id) {
        return planSuscripcionService.obtenerPorId(id);
    }

    @PostMapping
    public ResponseEntity<PlanSuscripcionResponse> crear(@Valid @RequestBody PlanSuscripcionRequest request) {
        PlanSuscripcionResponse creado = planSuscripcionService.crear(request);
        URI ubicacion = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(creado.idPlan()).toUri();
        return ResponseEntity.created(ubicacion).body(creado);
    }

    @PutMapping("/{id}")
    public PlanSuscripcionResponse actualizar(@PathVariable Long id, @Valid @RequestBody PlanSuscripcionRequest request) {
        return planSuscripcionService.actualizar(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id) {
        planSuscripcionService.eliminar(id);
    }
}
