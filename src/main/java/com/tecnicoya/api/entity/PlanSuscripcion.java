package com.tecnicoya.api.entity;

import com.tecnicoya.api.entity.enums.EstadoRegistro;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/** Plan que la plataforma ofrece a los técnicos: cobra {@code precioInicial} hasta {@code limiteClientes} clientes y {@code precioPosterior} a partir de ahí (tabla {@code plan_suscripcion}). */
@Entity
@Table(name = "plan_suscripcion")
@Getter
@Setter
@NoArgsConstructor
public class PlanSuscripcion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idPlan")
    private Long idPlan;

    @Column(name = "nombre_plan", nullable = false, unique = true, length = 100)
    private String nombrePlan;

    @Column(name = "precio_inicial", nullable = false, precision = 10, scale = 2)
    private BigDecimal precioInicial;

    @Column(name = "limite_clientes", nullable = false)
    private Integer limiteClientes;

    @Column(name = "precio_posterior", nullable = false, precision = 10, scale = 2)
    private BigDecimal precioPosterior;

    @Column(name = "descripcion", length = 500)
    private String descripcion;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false)
    private EstadoRegistro estado;
}
