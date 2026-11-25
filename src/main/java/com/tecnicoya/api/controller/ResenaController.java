package com.tecnicoya.api.controller;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/resenas")
public class ResenaController {

    private final List<Map<String, Object>> resenas = new CopyOnWriteArrayList<>();

    public ResenaController() {
        resenas.add(Map.of(
                "id", 1,
                "tecnicoId", 10,
                "calificacion", 5,
                "comentario", "Diagnóstico rápido y solución limpia.",
                "fecha", LocalDateTime.now().toString()
        ));
    }

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> listarResenas() {
        return ResponseEntity.ok(List.copyOf(resenas));
    }

    @GetMapping("/tecnico/{tecnicoId}")
    public ResponseEntity<List<Map<String, Object>>> listarPorTecnico(@PathVariable int tecnicoId) {
        List<Map<String, Object>> filtradas = new ArrayList<>();
        for (Map<String, Object> r : resenas) {
            if (String.valueOf(r.get("tecnicoId")).equals(String.valueOf(tecnicoId))) {
                filtradas.add(r);
            }
        }
        return ResponseEntity.ok(filtradas);
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> registrarResena(@RequestBody Map<String, Object> body) {
        int nextId = resenas.size() + 1;
        Map<String, Object> nueva = Map.of(
                "id", nextId,
                "tecnicoId", body.getOrDefault("tecnicoId", 1),
                "calificacion", body.getOrDefault("calificacion", 5),
                "comentario", body.getOrDefault("comentario", ""),
                "fecha", LocalDateTime.now().toString()
        );
        resenas.add(nueva);
        return ResponseEntity.status(HttpStatus.CREATED).body(nueva);
    }
}