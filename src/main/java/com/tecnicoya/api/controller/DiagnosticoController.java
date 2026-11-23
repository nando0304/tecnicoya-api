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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/diagnosticos")
public class DiagnosticoController {

    private final List> diagnosticos = new CopyOnWriteArrayList<>();

    public DiagnosticoController() {
        Map<String, Object> inicial = new LinkedHashMap<>();
        inicial.put("id", 1);
        inicial.put("ticketId", "TCK-1001");
        inicial.put("causa", "Conflicto de controlador tras actualización del sistema.");
        inicial.put("solucion", "Reversión de driver y limpieza de caché.");
        inicial.put("fecha", LocalDateTime.now().toString());
        diagnosticos.add(inicial);
    }

    @GetMapping("/ticket/{ticketId}")
    public ResponseEntity<List>> obtenerPorTicket(@PathVariable String ticketId) {
        List> resultado = new ArrayList<>();
        for (Map<String, Object> dg : diagnosticos) {
            if (String.valueOf(dg.get("ticketId")).equalsIgnoreCase(ticketId)) {
                resultado.add(dg);
            }
        }
        return ResponseEntity.ok(resultado);
    }

    @PostMapping
    public ResponseEntity> registrar(@RequestBody Map<String, Object> body) {
        Map<String, Object> nuevo = new LinkedHashMap<>();
        nuevo.put("id", diagnosticos.size() + 1);
        nuevo.put("ticketId", body.getOrDefault("ticketId", "TCK-1001"));
        nuevo.put("causa", body.getOrDefault("causa", "Falla por determinar"));
        nuevo.put("solucion", body.getOrDefault("solucion", "En revisión"));
        nuevo.put("fecha", LocalDateTime.now().toString());
        diagnosticos.add(nuevo);
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevo);
    }
}