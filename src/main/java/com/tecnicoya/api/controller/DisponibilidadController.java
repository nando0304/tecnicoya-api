package com.tecnicoya.api.controller;

import com.tecnicoya.api.dto.request.DisponibilidadRequest;
import com.tecnicoya.api.dto.response.DisponibilidadResponse;
import com.tecnicoya.api.service.DisponibilidadService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

/**
 * Endpoints REST de franjas de disponibilidad de los técnicos, bajo {@code /api/disponibilidades}.
 *
 * <p>Las reglas de negocio se aplican en {@link DisponibilidadService}.
 */
@RestController
@RequestMapping("/api/disponibilidades")
@RequiredArgsConstructor
public class DisponibilidadController {

    private final DisponibilidadService disponibilidadService;

    /**
     * {@code GET /api/disponibilidades}: lista todos los registros.
     *
     * @return 200 con la lista (vacía si no hay registros)
     */
    @GetMapping
    public List<DisponibilidadResponse> listar() {
        return disponibilidadService.listar();
    }

    /**
     * {@code GET /api/disponibilidades/{id}}: obtiene un registro.
     *
     * @param id id del registro
     * @return 200 con el registro; 404 si no existe
     */
    @GetMapping("/{id}")
    public DisponibilidadResponse obtenerPorId(@PathVariable Long id) {
        return disponibilidadService.obtenerPorId(id);
    }

    /**
     * {@code POST /api/disponibilidades}: crea un registro.
     *
     * @param request datos del nuevo registro
     * @return 201 con el registro creado y la cabecera {@code Location};
     *         400 si los datos son inválidos o incumplen una regla de negocio;
     *         404 si hace referencia a un registro inexistente; 409 si hay un conflicto
     */
    @PostMapping
    public ResponseEntity<DisponibilidadResponse> crear(@Valid @RequestBody DisponibilidadRequest request) {
        DisponibilidadResponse creado = disponibilidadService.crear(request);
        URI ubicacion = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(creado.idDisponibilidad()).toUri();
        return ResponseEntity.created(ubicacion).body(creado);
    }

    /**
     * {@code PUT /api/disponibilidades/{id}}: reemplaza los datos de un registro.
     *
     * @param id      id del registro
     * @param request nuevos datos
     * @return 200 con el registro actualizado; 400, 404 o 409 en los mismos casos que {@link #crear}
     */
    @PutMapping("/{id}")
    public DisponibilidadResponse actualizar(@PathVariable Long id, @Valid @RequestBody DisponibilidadRequest request) {
        return disponibilidadService.actualizar(id, request);
    }

    /**
     * {@code DELETE /api/disponibilidades/{id}}: elimina un registro.
     * Responde 204 sin contenido, 404 si no existe o 409 si otros registros dependen de él.
     *
     * @param id id del registro
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id) {
        disponibilidadService.eliminar(id);
    }
}
