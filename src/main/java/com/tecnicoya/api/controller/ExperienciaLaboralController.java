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

/**
 * Endpoints REST de experiencia laboral de los técnicos, bajo {@code /api/experiencias-laborales}.
 *
 * <p>Las reglas de negocio se aplican en {@link ExperienciaLaboralService}.
 */
@RestController
@RequestMapping("/api/experiencias-laborales")
@RequiredArgsConstructor
public class ExperienciaLaboralController {

    private final ExperienciaLaboralService experienciaLaboralService;

    /**
     * {@code GET /api/experiencias-laborales}: lista todos los registros.
     *
     * @return 200 con la lista (vacía si no hay registros)
     */
    @GetMapping
    public List<ExperienciaLaboralResponse> listar() {
        return experienciaLaboralService.listar();
    }

    /**
     * {@code GET /api/experiencias-laborales/{id}}: obtiene un registro.
     *
     * @param id id del registro
     * @return 200 con el registro; 404 si no existe
     */
    @GetMapping("/{id}")
    public ExperienciaLaboralResponse obtenerPorId(@PathVariable Long id) {
        return experienciaLaboralService.obtenerPorId(id);
    }

    /**
     * {@code POST /api/experiencias-laborales}: crea un registro.
     *
     * @param request datos del nuevo registro
     * @return 201 con el registro creado y la cabecera {@code Location};
     *         400 si los datos son inválidos o incumplen una regla de negocio;
     *         404 si hace referencia a un registro inexistente; 409 si hay un conflicto
     */
    @PostMapping
    public ResponseEntity<ExperienciaLaboralResponse> crear(@Valid @RequestBody ExperienciaLaboralRequest request) {
        ExperienciaLaboralResponse creado = experienciaLaboralService.crear(request);
        URI ubicacion = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(creado.idExperiencia()).toUri();
        return ResponseEntity.created(ubicacion).body(creado);
    }

    /**
     * {@code PUT /api/experiencias-laborales/{id}}: reemplaza los datos de un registro.
     *
     * @param id      id del registro
     * @param request nuevos datos
     * @return 200 con el registro actualizado; 400, 404 o 409 en los mismos casos que {@link #crear}
     */
    @PutMapping("/{id}")
    public ExperienciaLaboralResponse actualizar(@PathVariable Long id, @Valid @RequestBody ExperienciaLaboralRequest request) {
        return experienciaLaboralService.actualizar(id, request);
    }

    /**
     * {@code DELETE /api/experiencias-laborales/{id}}: elimina un registro.
     * Responde 204 sin contenido, 404 si no existe o 409 si otros registros dependen de él.
     *
     * @param id id del registro
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id) {
        experienciaLaboralService.eliminar(id);
    }
}
