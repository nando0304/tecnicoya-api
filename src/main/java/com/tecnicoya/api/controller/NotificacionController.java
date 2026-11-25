package com.tecnicoya.api.controller;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notificaciones")
public class NotificacionController {

    private final List<Map<String, Object>> notificaciones = new CopyOnWriteArrayList<>();

    public NotificacionController() {
        Map<String, Object> inicial = new LinkedHashMap<>();
        inicial.put("id", 1);
        inicial.put("usuarioId", 1);
        inicial.put("mensaje", "Tu ticket TCK-1001 ha sido recibido.");
        inicial.put("leida", false);
        inicial.put("fecha", LocalDateTime.now().toString());
        notificaciones.add(inicial);
    }

    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<List<Map<String, Object>>> listarPorUsuario(@PathVariable int usuarioId) {
        List<Map<String, Object>> resultado = new ArrayList<>();
        for (Map<String, Object> n : notificaciones) {
            if (String.valueOf(n.get("usuarioId")).equals(String.valueOf(usuarioId))) {
                resultado.add(n);
            }
        }
        return ResponseEntity.ok(resultado);
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> crear(@RequestBody Map<String, Object> body) {
        Map<String, Object> nueva = new LinkedHashMap<>();
        nueva.put("id", notificaciones.size() + 1);
        nueva.put("usuarioId", body.getOrDefault("usuarioId", 1));
        nueva.put("mensaje", body.getOrDefault("mensaje", "Nueva notificación del sistema."));
        nueva.put("leida", false);
        nueva.put("fecha", LocalDateTime.now().toString());
        notificaciones.add(nueva);
        return ResponseEntity.status(HttpStatus.CREATED).body(nueva);
    }

    @PutMapping("/{id}/leida")
    public ResponseEntity<?> marcarLeida(@PathVariable int id) {
        for (Map<String, Object> n : notificaciones) {
            if (Integer.parseInt(String.valueOf(n.get("id"))) == id) {
                n.put("leida", true);
                return ResponseEntity.ok(n);
            }
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", "Notificación no encontrada", "id", id));
    }
}