package com.tecnicoya.api.controller;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dispositivos")
public class DispositivoController {

    private final List<Map<String, Object>> dispositivos = new CopyOnWriteArrayList<>();

    public DispositivoController() {
        Map<String, Object> inicial = new LinkedHashMap<>();
        inicial.put("id", 1);
        inicial.put("usuarioId", 1);
        inicial.put("tipo", "Laptop");
        inicial.put("marca", "Lenovo");
        inicial.put("modelo", "IdeaPad 3");
        dispositivos.add(inicial);
    }

    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<List<Map<String, Object>>> listarPorUsuario(@PathVariable int usuarioId) {
        List<Map<String, Object>> resultado = new ArrayList<>();
        for (Map<String, Object> d : dispositivos) {
            if (String.valueOf(d.get("usuarioId")).equals(String.valueOf(usuarioId))) {
                resultado.add(d);
            }
        }
        return ResponseEntity.ok(resultado);
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> registrar(@RequestBody Map<String, Object> body) {
        Map<String, Object> nuevo = new LinkedHashMap<>();
        nuevo.put("id", dispositivos.size() + 1);
        nuevo.put("usuarioId", body.getOrDefault("usuarioId", 1));
        nuevo.put("tipo", body.getOrDefault("tipo", "Laptop"));
        nuevo.put("marca", body.getOrDefault("marca", "Genérica"));
        nuevo.put("modelo", body.getOrDefault("modelo", "Estándar"));
        dispositivos.add(nuevo);
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevo);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(@PathVariable int id) {
        for (Map<String, Object> d : dispositivos) {
            if (Integer.parseInt(String.valueOf(d.get("id"))) == id) {
                dispositivos.remove(d);
                return ResponseEntity.ok(Map.of("mensaje", "Dispositivo eliminado", "id", id));
            }
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", "Dispositivo no encontrado", "id", id));
    }
}