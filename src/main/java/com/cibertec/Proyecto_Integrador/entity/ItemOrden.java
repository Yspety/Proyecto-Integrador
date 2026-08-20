package com.cibertec.Proyecto_Integrador.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Línea de un pedido. Congela precio y nombre al momento del checkout.
 *
 * <p>El snapshot no es redundancia: si mañana el producto sube de precio o cambia de
 * nombre, el comprobante ya emitido tiene que seguir mostrando lo que el cliente
 * realmente compró. Se conserva igual la FK al producto para el kardex y los reportes.
 */
@Entity
@Table(name = "order_item")
@Getter
@Setter
@NoArgsConstructor
public class ItemOrden {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private Orden order;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Producto product;

    /** Snapshot del nombre al comprar. */
    @Column(name = "product_name", nullable = false)
    private String productName;

    @Column(nullable = false)
    private int quantity;

    /** Snapshot del precio unitario (IGV incluido). */
    @Column(name = "unit_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal unitPrice;

    /** {@code unitPrice * quantity}, persistido para no recalcularlo en cada lectura. */
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal subtotal;
}
