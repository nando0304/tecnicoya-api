package com.tecnicoya.api.controller;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/soporte")
public class SoporteController {

    private final Map<String, Map<String, Object>> tickets = new ConcurrentHashMap<>();

    public SoporteController() {
        tickets.put("TCK-1001", Map.of(
                "id", "TCK-1001",
                "titulo", "No enciende laptop tras actualización",
                "categoria", "Hardware",
                "estado", "ABIERTO",
                "creadoEn", LocalDateTime.now().toString()
        ));
    }

    @GetMapping("/tickets")
    public ResponseEntity<List>> listarTickets() {
        return ResponseEntity.ok(List.copyOf(tickets.values()));
    }

    @GetMapping("/tickets/{id}")
    public ResponseEntity<?> obtenerTicket(@PathVariable String id) {
        Map<String, Object> ticket = tickets.get(id);
        if (ticket == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Ticket no encontrado", "id", id));
        }
        return ResponseEntity.ok(ticket);
    }

    @PostMapping("/tickets")
    public ResponseEntity> crearTicket(@RequestBody Map<String, Object> body) {
        String id = "TCK-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        Map<String, Object> nuevoTicket = Map.of(
                "id", id,
                "titulo", body.getOrDefault("titulo", "Solicitud de soporte técnico"),
                "categoria", body.getOrDefault("categoria", "General"),
                "estado", "ABIERTO",
                "creadoEn", LocalDateTime.now().toString()
        );
        tickets.put(id, nuevoTicket);
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevoTicket);
    }
}