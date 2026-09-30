package com.tecnicoya.api.controller;

import com.tecnicoya.api.dto.request.ServicioRequest;
import com.tecnicoya.api.dto.response.ServicioResponse;
import com.tecnicoya.api.service.ServicioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

/**
 * Endpoints REST de servicios solicitados por los clientes, bajo {@code /api/servicios}.
 *
 * <p>Las reglas de negocio se aplican en {@link ServicioService}.
 */
@RestController
@RequestMapping("/api/servicios")
@RequiredArgsConstructor
public class ServicioController {

    private final ServicioService servicioService;

    /**
     * {@code GET /api/servicios}: lista todos los registros.
     *
     * @return 200 con la lista (vacía si no hay registros)
     */
    @GetMapping
    public List<ServicioResponse> listar() {
        return servicioService.listar();
    }

    /**
     * {@code GET /api/servicios/{id}}: obtiene un registro.
     *
     * @param id id del registro
     * @return 200 con el registro; 404 si no existe
     */
    @GetMapping("/{id}")
    public ServicioResponse obtenerPorId(@PathVariable Long id) {
        return servicioService.obtenerPorId(id);
    }

    /**
     * {@code POST /api/servicios}: crea un registro.
     *
     * @param request datos del nuevo registro
     * @return 201 con el registro creado y la cabecera {@code Location};
     *         400 si los datos son inválidos o incumplen una regla de negocio;
     *         404 si hace referencia a un registro inexistente; 409 si hay un conflicto
     */
    @PostMapping
    public ResponseEntity<ServicioResponse> crear(@Valid @RequestBody ServicioRequest request) {
        ServicioResponse creado = servicioService.crear(request);
        URI ubicacion = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(creado.idServicio()).toUri();
        return ResponseEntity.created(ubicacion).body(creado);
    }

    /**
     * {@code PUT /api/servicios/{id}}: reemplaza los datos de un registro.
     *
     * @param id      id del registro
     * @param request nuevos datos
     * @return 200 con el registro actualizado; 400, 404 o 409 en los mismos casos que {@link #crear}
     */
    @PutMapping("/{id}")
    public ServicioResponse actualizar(@PathVariable Long id, @Valid @RequestBody ServicioRequest request) {
        return servicioService.actualizar(id, request);
    }

    /**
     * {@code DELETE /api/servicios/{id}}: elimina un registro.
     * Responde 204 sin contenido, 404 si no existe o 409 si otros registros dependen de él.
     *
     * @param id id del registro
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id) {
        servicioService.eliminar(id);
    }
}
