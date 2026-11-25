package com.tecnicoya.api.controller;

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
@RequestMapping("/api/citas")
public class CitaController {

    private final List<Map<String, Object>> citas = new CopyOnWriteArrayList<>();

    public CitaController() {
        Map<String, Object> inicial = new LinkedHashMap<>();
        inicial.put("id", 1);
        inicial.put("tecnicoId", 10);
        inicial.put("modalidad", "REMOTA");
        inicial.put("fechaHora", "2026-11-24T10:00:00");
        inicial.put("estado", "PROGRAMADA");
        citas.add(inicial);
    }

    @GetMapping("/tecnico/{tecnicoId}")
    public ResponseEntity<List<Map<String, Object>>> listarPorTecnico(@PathVariable int tecnicoId) {
        List<Map<String, Object>> resultado = new ArrayList<>();
        for (Map<String, Object> c : citas) {
            if (String.valueOf(c.get("tecnicoId")).equals(String.valueOf(tecnicoId))) {
                resultado.add(c);
            }
        }
        return ResponseEntity.ok(resultado);
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> agendar(@RequestBody Map<String, Object> body) {
        Map<String, Object> nueva = new LinkedHashMap<>();
        nueva.put("id", citas.size() + 1);
        nueva.put("tecnicoId", body.getOrDefault("tecnicoId", 10));
        nueva.put("modalidad", body.getOrDefault("modalidad", "REMOTA"));
        nueva.put("fechaHora", body.getOrDefault("fechaHora", "2026-11-25T15:00:00"));
        nueva.put("estado", "PROGRAMADA");
        citas.add(nueva);
        return ResponseEntity.status(HttpStatus.CREATED).body(nueva);
    }

    @PutMapping("/{id}/reprogramar")
    public ResponseEntity<?> reprogramar(@PathVariable int id, @RequestBody Map<String, Object> body) {
        for (Map<String, Object> c : citas) {
            if (Integer.parseInt(String.valueOf(c.get("id"))) == id) {
                if (body.containsKey("fechaHora")) {
                    c.put("fechaHora", body.get("fechaHora"));
                }
                c.put("estado", "REPROGRAMADA");
                return ResponseEntity.ok(c);
            }
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", "Cita no encontrada", "id", id));
    }
}