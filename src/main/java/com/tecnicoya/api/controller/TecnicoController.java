package com.tecnicoya.api.controller;

import com.tecnicoya.api.dto.request.TecnicoRequest;
import com.tecnicoya.api.dto.response.TecnicoResponse;
import com.tecnicoya.api.service.TecnicoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

/** CRUD de perfiles de técnico. */
@RestController
@RequestMapping("/api/tecnicos")
@RequiredArgsConstructor
public class TecnicoController {

    private final TecnicoService tecnicoService;

    @GetMapping
    public List<TecnicoResponse> listar() {
        return tecnicoService.listar();
    }

    @GetMapping("/{id}")
    public TecnicoResponse obtenerPorId(@PathVariable Long id) {
        return tecnicoService.obtenerPorId(id);
    }

    @PostMapping
    public ResponseEntity<TecnicoResponse> crear(@Valid @RequestBody TecnicoRequest request) {
        TecnicoResponse creado = tecnicoService.crear(request);
        URI ubicacion = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(creado.idTecnico()).toUri();
        return ResponseEntity.created(ubicacion).body(creado);
    }

    @PutMapping("/{id}")
    public TecnicoResponse actualizar(@PathVariable Long id, @Valid @RequestBody TecnicoRequest request) {
        return tecnicoService.actualizar(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id) {
        tecnicoService.eliminar(id);
    }
}
