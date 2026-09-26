package com.tecnicoya.api.controller;

import com.tecnicoya.api.dto.request.EvidenciaPagoRequest;
import com.tecnicoya.api.dto.response.EvidenciaPagoResponse;
import com.tecnicoya.api.service.EvidenciaPagoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

/** CRUD de evidencias de pago de servicios. */
@RestController
@RequestMapping("/api/evidencias-pago")
@RequiredArgsConstructor
public class EvidenciaPagoController {

    private final EvidenciaPagoService evidenciaPagoService;

    @GetMapping
    public List<EvidenciaPagoResponse> listar() {
        return evidenciaPagoService.listar();
    }

    @GetMapping("/{id}")
    public EvidenciaPagoResponse obtenerPorId(@PathVariable Long id) {
        return evidenciaPagoService.obtenerPorId(id);
    }

    @PostMapping
    public ResponseEntity<EvidenciaPagoResponse> crear(@Valid @RequestBody EvidenciaPagoRequest request) {
        EvidenciaPagoResponse creado = evidenciaPagoService.crear(request);
        URI ubicacion = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(creado.idEvidenciaPago()).toUri();
        return ResponseEntity.created(ubicacion).body(creado);
    }

    @PutMapping("/{id}")
    public EvidenciaPagoResponse actualizar(@PathVariable Long id, @Valid @RequestBody EvidenciaPagoRequest request) {
        return evidenciaPagoService.actualizar(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id) {
        evidenciaPagoService.eliminar(id);
    }
}
