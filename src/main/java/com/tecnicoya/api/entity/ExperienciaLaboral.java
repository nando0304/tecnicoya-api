package com.tecnicoya.api.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "experiencia_laboral")
@Getter
@Setter
@NoArgsConstructor
public class ExperienciaLaboral {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idExperiencia")
    private Long idExperiencia;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tecnico_id", nullable = false)
    private Tecnico tecnico;

    @Column(name = "empresa", nullable = false, length = 150)
    private String empresa;

    @Column(name = "cargo", nullable = false, length = 100)
    private String cargo;

    @Column(name = "descripcion", length = 1000)
    private String descripcion;

    @Column(name = "fecha_inicio", nullable = false)
    private LocalDate fechaInicio;

    /** Nulo cuando el técnico aún trabaja allí (actualidad = true). */
    @Column(name = "fecha_fin")
    private LocalDate fechaFin;

    @Column(name = "actualidad", nullable = false)
    private Boolean actualidad;
}
