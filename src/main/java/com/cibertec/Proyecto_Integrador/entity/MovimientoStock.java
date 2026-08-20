package com.cibertec.Proyecto_Integrador.entity;

import jakarta.persistence.*;
import java.time.Instant;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import com.cibertec.Proyecto_Integrador.entity.enums.TipoMovimiento;

/**
 * Una línea del kardex: el historial auditable de por qué el stock de un producto
 * cambió. {@code Producto.stock} es el saldo actual; esta tabla es cómo se llegó ahí.
 *
 * <p>Append-only. Si algo sale mal se agrega el movimiento inverso, nunca se edita
 * la fila — de ahí que no haya setters de corrección ni borrado en el repositorio.
 */
@Entity
@Table(name = "stock_movement", indexes = {
        @Index(name = "idx_stock_movement_product", columnList = "product_id, created_at")
})
@Getter
@Setter
@NoArgsConstructor
public class MovimientoStock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Producto product;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private TipoMovimiento type;

    /** Siempre positiva: el sentido lo da {@link #type}, no el signo. */
    @Column(nullable = false)
    private int quantity;

    /** Motivo legible: "Checkout", "Cancelación de pedido". */
    @Column(nullable = false, length = 100)
    private String reason;

    /** Puntero al documento que lo originó: "ORDEN-42". */
    @Column(nullable = false, length = 50)
    private String reference;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}
