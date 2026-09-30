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

/**
 * Endpoints REST de perfiles de técnico, bajo {@code /api/tecnicos}.
 *
 * <p>Las reglas de negocio se aplican en {@link TecnicoService}.
 */
@RestController
@RequestMapping("/api/tecnicos")
@RequiredArgsConstructor
public class TecnicoController {

    private final TecnicoService tecnicoService;

    /**
     * {@code GET /api/tecnicos}: lista todos los registros.
     *
     * @return 200 con la lista (vacía si no hay registros)
     */
    @GetMapping
    public List<TecnicoResponse> listar() {
        return tecnicoService.listar();
    }

    /**
     * {@code GET /api/tecnicos/{id}}: obtiene un registro.
     *
     * @param id id del registro
     * @return 200 con el registro; 404 si no existe
     */
    @GetMapping("/{id}")
    public TecnicoResponse obtenerPorId(@PathVariable Long id) {
        return tecnicoService.obtenerPorId(id);
    }

    /**
     * {@code POST /api/tecnicos}: crea un registro.
     *
     * @param request datos del nuevo registro
     * @return 201 con el registro creado y la cabecera {@code Location};
     *         400 si los datos son inválidos o incumplen una regla de negocio;
     *         404 si hace referencia a un registro inexistente; 409 si hay un conflicto
     */
    @PostMapping
    public ResponseEntity<TecnicoResponse> crear(@Valid @RequestBody TecnicoRequest request) {
        TecnicoResponse creado = tecnicoService.crear(request);
        URI ubicacion = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(creado.idTecnico()).toUri();
        return ResponseEntity.created(ubicacion).body(creado);
    }

    /**
     * {@code PUT /api/tecnicos/{id}}: reemplaza los datos de un registro.
     *
     * @param id      id del registro
     * @param request nuevos datos
     * @return 200 con el registro actualizado; 400, 404 o 409 en los mismos casos que {@link #crear}
     */
    @PutMapping("/{id}")
    public TecnicoResponse actualizar(@PathVariable Long id, @Valid @RequestBody TecnicoRequest request) {
        return tecnicoService.actualizar(id, request);
    }

    /**
     * {@code DELETE /api/tecnicos/{id}}: elimina un registro.
     * Responde 204 sin contenido, 404 si no existe o 409 si otros registros dependen de él.
     *
     * @param id id del registro
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id) {
        tecnicoService.eliminar(id);
    }
}
