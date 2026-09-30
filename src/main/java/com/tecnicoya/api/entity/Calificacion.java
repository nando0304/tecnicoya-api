package com.tecnicoya.api.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/** Valoración de 1 a 5 que recibe un servicio finalizado; como máximo una por servicio (tabla {@code calificacion}). */
@Entity
@Table(name = "calificacion")
@Getter
@Setter
@NoArgsConstructor
public class Calificacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idCalificacion")
    private Long idCalificacion;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "servicio_id", nullable = false, unique = true)
    private Servicio servicio;

    @Column(name = "puntuacion", nullable = false)
    private Integer puntuacion;

    @Column(name = "comentario", length = 500)
    private String comentario;

    @Column(name = "fecha_calificacion", nullable = false, updatable = false)
    private LocalDateTime fechaCalificacion;
}
