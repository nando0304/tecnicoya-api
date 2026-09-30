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

/**
 * Endpoints REST de evidencias de pago de servicios, bajo {@code /api/evidencias-pago}.
 *
 * <p>Las reglas de negocio se aplican en {@link EvidenciaPagoService}.
 */
@RestController
@RequestMapping("/api/evidencias-pago")
@RequiredArgsConstructor
public class EvidenciaPagoController {

    private final EvidenciaPagoService evidenciaPagoService;

    /**
     * {@code GET /api/evidencias-pago}: lista todos los registros.
     *
     * @return 200 con la lista (vacía si no hay registros)
     */
    @GetMapping
    public List<EvidenciaPagoResponse> listar() {
        return evidenciaPagoService.listar();
    }

    /**
     * {@code GET /api/evidencias-pago/{id}}: obtiene un registro.
     *
     * @param id id del registro
     * @return 200 con el registro; 404 si no existe
     */
    @GetMapping("/{id}")
    public EvidenciaPagoResponse obtenerPorId(@PathVariable Long id) {
        return evidenciaPagoService.obtenerPorId(id);
    }

    /**
     * {@code POST /api/evidencias-pago}: crea un registro.
     *
     * @param request datos del nuevo registro
     * @return 201 con el registro creado y la cabecera {@code Location};
     *         400 si los datos son inválidos o incumplen una regla de negocio;
     *         404 si hace referencia a un registro inexistente; 409 si hay un conflicto
     */
    @PostMapping
    public ResponseEntity<EvidenciaPagoResponse> crear(@Valid @RequestBody EvidenciaPagoRequest request) {
        EvidenciaPagoResponse creado = evidenciaPagoService.crear(request);
        URI ubicacion = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(creado.idEvidenciaPago()).toUri();
        return ResponseEntity.created(ubicacion).body(creado);
    }

    /**
     * {@code PUT /api/evidencias-pago/{id}}: reemplaza los datos de un registro.
     *
     * @param id      id del registro
     * @param request nuevos datos
     * @return 200 con el registro actualizado; 400, 404 o 409 en los mismos casos que {@link #crear}
     */
    @PutMapping("/{id}")
    public EvidenciaPagoResponse actualizar(@PathVariable Long id, @Valid @RequestBody EvidenciaPagoRequest request) {
        return evidenciaPagoService.actualizar(id, request);
    }

    /**
     * {@code DELETE /api/evidencias-pago/{id}}: elimina un registro.
     * Responde 204 sin contenido, 404 si no existe o 409 si otros registros dependen de él.
     *
     * @param id id del registro
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id) {
        evidenciaPagoService.eliminar(id);
    }
}
