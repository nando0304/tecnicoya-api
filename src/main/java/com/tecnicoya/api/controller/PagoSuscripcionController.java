package com.tecnicoya.api.controller;

import com.tecnicoya.api.dto.request.PagoSuscripcionRequest;
import com.tecnicoya.api.dto.response.PagoSuscripcionResponse;
import com.tecnicoya.api.service.PagoSuscripcionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

/** CRUD de pagos de suscripción. */
@RestController
@RequestMapping("/api/pagos-suscripcion")
@RequiredArgsConstructor
public class PagoSuscripcionController {

    private final PagoSuscripcionService pagoSuscripcionService;

    @GetMapping
    public List<PagoSuscripcionResponse> listar() {
        return pagoSuscripcionService.listar();
    }

    @GetMapping("/{id}")
    public PagoSuscripcionResponse obtenerPorId(@PathVariable Long id) {
        return pagoSuscripcionService.obtenerPorId(id);
    }

    @PostMapping
    public ResponseEntity<PagoSuscripcionResponse> crear(@Valid @RequestBody PagoSuscripcionRequest request) {
        PagoSuscripcionResponse creado = pagoSuscripcionService.crear(request);
        URI ubicacion = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(creado.idPagoSuscripcion()).toUri();
        return ResponseEntity.created(ubicacion).body(creado);
    }

    @PutMapping("/{id}")
    public PagoSuscripcionResponse actualizar(@PathVariable Long id, @Valid @RequestBody PagoSuscripcionRequest request) {
        return pagoSuscripcionService.actualizar(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id) {
        pagoSuscripcionService.eliminar(id);
    }
}
