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

/**
 * Endpoints REST de pagos de suscripción, bajo {@code /api/pagos-suscripcion}.
 *
 * <p>Las reglas de negocio se aplican en {@link PagoSuscripcionService}.
 */
@RestController
@RequestMapping("/api/pagos-suscripcion")
@RequiredArgsConstructor
public class PagoSuscripcionController {

    private final PagoSuscripcionService pagoSuscripcionService;

    /**
     * {@code GET /api/pagos-suscripcion}: lista todos los registros.
     *
     * @return 200 con la lista (vacía si no hay registros)
     */
    @GetMapping
    public List<PagoSuscripcionResponse> listar() {
        return pagoSuscripcionService.listar();
    }

    /**
     * {@code GET /api/pagos-suscripcion/{id}}: obtiene un registro.
     *
     * @param id id del registro
     * @return 200 con el registro; 404 si no existe
     */
    @GetMapping("/{id}")
    public PagoSuscripcionResponse obtenerPorId(@PathVariable Long id) {
        return pagoSuscripcionService.obtenerPorId(id);
    }

    /**
     * {@code POST /api/pagos-suscripcion}: crea un registro.
     *
     * @param request datos del nuevo registro
     * @return 201 con el registro creado y la cabecera {@code Location};
     *         400 si los datos son inválidos o incumplen una regla de negocio;
     *         404 si hace referencia a un registro inexistente; 409 si hay un conflicto
     */
    @PostMapping
    public ResponseEntity<PagoSuscripcionResponse> crear(@Valid @RequestBody PagoSuscripcionRequest request) {
        PagoSuscripcionResponse creado = pagoSuscripcionService.crear(request);
        URI ubicacion = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(creado.idPagoSuscripcion()).toUri();
        return ResponseEntity.created(ubicacion).body(creado);
    }

    /**
     * {@code PUT /api/pagos-suscripcion/{id}}: reemplaza los datos de un registro.
     *
     * @param id      id del registro
     * @param request nuevos datos
     * @return 200 con el registro actualizado; 400, 404 o 409 en los mismos casos que {@link #crear}
     */
    @PutMapping("/{id}")
    public PagoSuscripcionResponse actualizar(@PathVariable Long id, @Valid @RequestBody PagoSuscripcionRequest request) {
        return pagoSuscripcionService.actualizar(id, request);
    }

    /**
     * {@code DELETE /api/pagos-suscripcion/{id}}: elimina un registro.
     * Responde 204 sin contenido, 404 si no existe o 409 si otros registros dependen de él.
     *
     * @param id id del registro
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id) {
        pagoSuscripcionService.eliminar(id);
    }
}
