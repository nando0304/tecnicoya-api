package com.tecnicoya.api.controller;

import com.tecnicoya.api.dto.request.UsuarioRequest;
import com.tecnicoya.api.dto.response.UsuarioResponse;
import com.tecnicoya.api.service.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

/**
 * Endpoints REST de usuarios (clientes, técnicos y administradores), bajo {@code /api/usuarios}.
 *
 * <p>Las reglas de negocio se aplican en {@link UsuarioService}.
 */
@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;

    /**
     * {@code GET /api/usuarios}: lista todos los registros.
     *
     * @return 200 con la lista (vacía si no hay registros)
     */
    @GetMapping
    public List<UsuarioResponse> listar() {
        return usuarioService.listar();
    }

    /**
     * {@code GET /api/usuarios/{id}}: obtiene un registro.
     *
     * @param id id del registro
     * @return 200 con el registro; 404 si no existe
     */
    @GetMapping("/{id}")
    public UsuarioResponse obtenerPorId(@PathVariable Long id) {
        return usuarioService.obtenerPorId(id);
    }

    /**
     * {@code POST /api/usuarios}: crea un registro.
     *
     * @param request datos del nuevo registro
     * @return 201 con el registro creado y la cabecera {@code Location};
     *         400 si los datos son inválidos o incumplen una regla de negocio;
     *         404 si hace referencia a un registro inexistente; 409 si hay un conflicto
     */
    @PostMapping
    public ResponseEntity<UsuarioResponse> crear(@Valid @RequestBody UsuarioRequest request) {
        UsuarioResponse creado = usuarioService.crear(request);
        URI ubicacion = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(creado.idUsuario()).toUri();
        return ResponseEntity.created(ubicacion).body(creado);
    }

    /**
     * {@code PUT /api/usuarios/{id}}: reemplaza los datos de un registro.
     *
     * @param id      id del registro
     * @param request nuevos datos
     * @return 200 con el registro actualizado; 400, 404 o 409 en los mismos casos que {@link #crear}
     */
    @PutMapping("/{id}")
    public UsuarioResponse actualizar(@PathVariable Long id, @Valid @RequestBody UsuarioRequest request) {
        return usuarioService.actualizar(id, request);
    }

    /**
     * {@code DELETE /api/usuarios/{id}}: elimina un registro.
     * Responde 204 sin contenido, 404 si no existe o 409 si otros registros dependen de él.
     *
     * @param id id del registro
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id) {
        usuarioService.eliminar(id);
    }
}
