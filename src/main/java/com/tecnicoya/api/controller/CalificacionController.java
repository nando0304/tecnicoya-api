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

/**
 * Endpoints REST de calificaciones de servicios, bajo {@code /api/calificaciones}.
 *
 * <p>Las reglas de negocio se aplican en {@link CalificacionService}.
 */
@RestController
@RequestMapping("/api/calificaciones")
@RequiredArgsConstructor
public class CalificacionController {

    private final CalificacionService calificacionService;

    /**
     * {@code GET /api/calificaciones}: lista todos los registros.
     *
     * @return 200 con la lista (vacía si no hay registros)
     */
    @GetMapping
    public List<CalificacionResponse> listar() {
        return calificacionService.listar();
    }

    /**
     * {@code GET /api/calificaciones/{id}}: obtiene un registro.
     *
     * @param id id del registro
     * @return 200 con el registro; 404 si no existe
     */
    @GetMapping("/{id}")
    public CalificacionResponse obtenerPorId(@PathVariable Long id) {
        return calificacionService.obtenerPorId(id);
    }

    /**
     * {@code POST /api/calificaciones}: crea un registro.
     *
     * @param request datos del nuevo registro
     * @return 201 con el registro creado y la cabecera {@code Location};
     *         400 si los datos son inválidos o incumplen una regla de negocio;
     *         404 si hace referencia a un registro inexistente; 409 si hay un conflicto
     */
    @PostMapping
    public ResponseEntity<CalificacionResponse> crear(@Valid @RequestBody CalificacionRequest request) {
        CalificacionResponse creado = calificacionService.crear(request);
        URI ubicacion = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(creado.idCalificacion()).toUri();
        return ResponseEntity.created(ubicacion).body(creado);
    }

    /**
     * {@code PUT /api/calificaciones/{id}}: reemplaza los datos de un registro.
     *
     * @param id      id del registro
     * @param request nuevos datos
     * @return 200 con el registro actualizado; 400, 404 o 409 en los mismos casos que {@link #crear}
     */
    @PutMapping("/{id}")
    public CalificacionResponse actualizar(@PathVariable Long id, @Valid @RequestBody CalificacionRequest request) {
        return calificacionService.actualizar(id, request);
    }

    /**
     * {@code DELETE /api/calificaciones/{id}}: elimina un registro.
     * Responde 204 sin contenido, 404 si no existe o 409 si otros registros dependen de él.
     *
     * @param id id del registro
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id) {
        calificacionService.eliminar(id);
    }
}
