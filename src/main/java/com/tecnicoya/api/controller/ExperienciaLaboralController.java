package com.tecnicoya.api.controller;

import com.tecnicoya.api.dto.request.ExperienciaLaboralRequest;
import com.tecnicoya.api.dto.response.ExperienciaLaboralResponse;
import com.tecnicoya.api.service.ExperienciaLaboralService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

/** CRUD de experiencia laboral de los técnicos. */
@RestController
@RequestMapping("/api/experiencias-laborales")
@RequiredArgsConstructor
public class ExperienciaLaboralController {

    private final ExperienciaLaboralService experienciaLaboralService;

    @GetMapping
    public List<ExperienciaLaboralResponse> listar() {
        return experienciaLaboralService.listar();
    }

    @GetMapping("/{id}")
    public ExperienciaLaboralResponse obtenerPorId(@PathVariable Long id) {
        return experienciaLaboralService.obtenerPorId(id);
    }

    @PostMapping
    public ResponseEntity<ExperienciaLaboralResponse> crear(@Valid @RequestBody ExperienciaLaboralRequest request) {
        ExperienciaLaboralResponse creado = experienciaLaboralService.crear(request);
        URI ubicacion = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(creado.idExperiencia()).toUri();
        return ResponseEntity.created(ubicacion).body(creado);
    }

    @PutMapping("/{id}")
    public ExperienciaLaboralResponse actualizar(@PathVariable Long id, @Valid @RequestBody ExperienciaLaboralRequest request) {
        return experienciaLaboralService.actualizar(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id) {
        experienciaLaboralService.eliminar(id);
    }
}
