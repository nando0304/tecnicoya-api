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

/**
 * Endpoints REST de suscripciones de técnicos a planes, bajo {@code /api/suscripciones}.
 *
 * <p>Las reglas de negocio se aplican en {@link SuscripcionService}.
 */
@RestController
@RequestMapping("/api/suscripciones")
@RequiredArgsConstructor
public class SuscripcionController {

    private final SuscripcionService suscripcionService;

    /**
     * {@code GET /api/suscripciones}: lista todos los registros.
     *
     * @return 200 con la lista (vacía si no hay registros)
     */
    @GetMapping
    public List<SuscripcionResponse> listar() {
        return suscripcionService.listar();
    }

    /**
     * {@code GET /api/suscripciones/{id}}: obtiene un registro.
     *
     * @param id id del registro
     * @return 200 con el registro; 404 si no existe
     */
    @GetMapping("/{id}")
    public SuscripcionResponse obtenerPorId(@PathVariable Long id) {
        return suscripcionService.obtenerPorId(id);
    }

    /**
     * {@code POST /api/suscripciones}: crea un registro.
     *
     * @param request datos del nuevo registro
     * @return 201 con el registro creado y la cabecera {@code Location};
     *         400 si los datos son inválidos o incumplen una regla de negocio;
     *         404 si hace referencia a un registro inexistente; 409 si hay un conflicto
     */
    @PostMapping
    public ResponseEntity<SuscripcionResponse> crear(@Valid @RequestBody SuscripcionRequest request) {
        SuscripcionResponse creado = suscripcionService.crear(request);
        URI ubicacion = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(creado.idSuscripcion()).toUri();
        return ResponseEntity.created(ubicacion).body(creado);
    }

    /**
     * {@code PUT /api/suscripciones/{id}}: reemplaza los datos de un registro.
     *
     * @param id      id del registro
     * @param request nuevos datos
     * @return 200 con el registro actualizado; 400, 404 o 409 en los mismos casos que {@link #crear}
     */
    @PutMapping("/{id}")
    public SuscripcionResponse actualizar(@PathVariable Long id, @Valid @RequestBody SuscripcionRequest request) {
        return suscripcionService.actualizar(id, request);
    }

    /**
     * {@code DELETE /api/suscripciones/{id}}: elimina un registro.
     * Responde 204 sin contenido, 404 si no existe o 409 si otros registros dependen de él.
     *
     * @param id id del registro
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id) {
        suscripcionService.eliminar(id);
    }
}
