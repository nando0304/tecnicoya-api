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

/**
 * Endpoints REST de planes de suscripción, bajo {@code /api/planes-suscripcion}.
 *
 * <p>Las reglas de negocio se aplican en {@link PlanSuscripcionService}.
 */
@RestController
@RequestMapping("/api/planes-suscripcion")
@RequiredArgsConstructor
public class PlanSuscripcionController {

    private final PlanSuscripcionService planSuscripcionService;

    /**
     * {@code GET /api/planes-suscripcion}: lista todos los registros.
     *
     * @return 200 con la lista (vacía si no hay registros)
     */
    @GetMapping
    public List<PlanSuscripcionResponse> listar() {
        return planSuscripcionService.listar();
    }

    /**
     * {@code GET /api/planes-suscripcion/{id}}: obtiene un registro.
     *
     * @param id id del registro
     * @return 200 con el registro; 404 si no existe
     */
    @GetMapping("/{id}")
    public PlanSuscripcionResponse obtenerPorId(@PathVariable Long id) {
        return planSuscripcionService.obtenerPorId(id);
    }

    /**
     * {@code POST /api/planes-suscripcion}: crea un registro.
     *
     * @param request datos del nuevo registro
     * @return 201 con el registro creado y la cabecera {@code Location};
     *         400 si los datos son inválidos o incumplen una regla de negocio;
     *         404 si hace referencia a un registro inexistente; 409 si hay un conflicto
     */
    @PostMapping
    public ResponseEntity<PlanSuscripcionResponse> crear(@Valid @RequestBody PlanSuscripcionRequest request) {
        PlanSuscripcionResponse creado = planSuscripcionService.crear(request);
        URI ubicacion = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(creado.idPlan()).toUri();
        return ResponseEntity.created(ubicacion).body(creado);
    }

    /**
     * {@code PUT /api/planes-suscripcion/{id}}: reemplaza los datos de un registro.
     *
     * @param id      id del registro
     * @param request nuevos datos
     * @return 200 con el registro actualizado; 400, 404 o 409 en los mismos casos que {@link #crear}
     */
    @PutMapping("/{id}")
    public PlanSuscripcionResponse actualizar(@PathVariable Long id, @Valid @RequestBody PlanSuscripcionRequest request) {
        return planSuscripcionService.actualizar(id, request);
    }

    /**
     * {@code DELETE /api/planes-suscripcion/{id}}: elimina un registro.
     * Responde 204 sin contenido, 404 si no existe o 409 si otros registros dependen de él.
     *
     * @param id id del registro
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id) {
        planSuscripcionService.eliminar(id);
    }
}
