package com.tecnicoya.api.entity;

import com.tecnicoya.api.entity.enums.EstadoPago;
import com.tecnicoya.api.entity.enums.MetodoPago;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Pago de un técnico por su suscripción, procesado por el proveedor de pagos (tabla {@code pago_suscripcion}). */
@Entity
@Table(name = "pago_suscripcion")
@Getter
@Setter
@NoArgsConstructor
public class PagoSuscripcion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idPagoSuscripcion")
    private Long idPagoSuscripcion;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "suscripcion_id", nullable = false)
    private Suscripcion suscripcion;

    @Column(name = "monto_pago", nullable = false, precision = 10, scale = 2)
    private BigDecimal montoPago;

    @Enumerated(EnumType.STRING)
    @Column(name = "metodo_pago", nullable = false)
    private MetodoPago metodoPago;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_pago", nullable = false)
    private EstadoPago estadoPago;

    @Column(name = "fecha_pago", nullable = false)
    private LocalDateTime fechaPago;
}
