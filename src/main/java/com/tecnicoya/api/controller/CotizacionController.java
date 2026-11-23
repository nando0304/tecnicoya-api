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
@RequestMapping("/api/cotizaciones")
public class CotizacionController {

    private final List> cotizaciones = new CopyOnWriteArrayList<>();

    public CotizacionController() {
        Map<String, Object> inicial = new LinkedHashMap<>();
        inicial.put("id", 1);
        inicial.put("ticketId", "TCK-1001");
        inicial.put("detalle", "Mantenimiento preventivo y optimización de sistema");
        inicial.put("monto", 65.00);
        inicial.put("estado", "PENDIENTE");
        cotizaciones.add(inicial);
    }

    @GetMapping("/ticket/{ticketId}")
    public ResponseEntity<List>> listarPorTicket(@PathVariable String ticketId) {
        List> resultado = new ArrayList<>();
        for (Map<String, Object> ct : cotizaciones) {
            if (String.valueOf(ct.get("ticketId")).equalsIgnoreCase(ticketId)) {
                resultado.add(ct);
            }
        }
        return ResponseEntity.ok(resultado);
    }

    @PostMapping
    public ResponseEntity> crear(@RequestBody Map<String, Object> body) {
        Map<String, Object> nueva = new LinkedHashMap<>();
        nueva.put("id", cotizaciones.size() + 1);
        nueva.put("ticketId", body.getOrDefault("ticketId", "TCK-1001"));
        nueva.put("detalle", body.getOrDefault("detalle", "Servicio técnico"));
        nueva.put("monto", body.getOrDefault("monto", 50.00));
        nueva.put("estado", "PENDIENTE");
        cotizaciones.add(nueva);
        return ResponseEntity.status(HttpStatus.CREATED).body(nueva);
    }

    @PutMapping("/{id}/aprobar")
    public ResponseEntity<?> aprobar(@PathVariable int id) {
        for (Map<String, Object> ct : cotizaciones) {
            if (Integer.parseInt(String.valueOf(ct.get("id"))) == id) {
                ct.put("estado", "APROBADA");
                return ResponseEntity.ok(ct);
            }
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", "Cotización no encontrada", "id", id));
    }
}