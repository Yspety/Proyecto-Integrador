package com.cibertec.Proyecto_Integrador.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import com.cibertec.Proyecto_Integrador.entity.enums.EstadoOrden;
import com.cibertec.Proyecto_Integrador.entity.enums.MetodoPago;
import com.cibertec.Proyecto_Integrador.entity.enums.TipoDocumento;

/**
 * Pedido confirmado. A diferencia del carrito, es un documento INMUTABLE en sus montos:
 * una vez creado, cambiar el precio del producto en el catálogo no lo altera. Por eso
 * cada {@link ItemOrden} congela su propio {@code unitPrice}.
 *
 * <p>Los importes ya vienen con IGV incluido (así se cargan los precios del catálogo).
 * El {@code igv} se desglosa HACIA ADENTRO del total, no se suma encima:
 * {@code base = total / 1.18} y {@code igv = total - base}. Se persisten todos los
 * montos en vez de recalcularlos al leer, porque la tasa de IGV o el costo de envío
 * pueden cambiar y un comprobante emitido no puede cambiar con ellos.
 *
 * <p>{@code orders} y no {@code order}: ORDER es palabra reservada en SQL.
 */
@Entity
@Table(name = "orders", indexes = {
        @Index(name = "idx_orders_user_date", columnList = "user_id, order_date"),
        @Index(name = "idx_orders_status", columnList = "status")
})
@Getter
@Setter
@NoArgsConstructor
public class Orden {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private Usuario user;

    @Column(name = "order_date", nullable = false, updatable = false)
    private Instant orderDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoOrden status;

    // ─── Comprobante ────────────────────────────────────────────────────────────

    @Enumerated(EnumType.STRING)
    @Column(name = "document_type", nullable = false, length = 10)
    private TipoDocumento documentType;

    /** Nombre (boleta) o razón social (factura). Snapshot: no sigue al usuario si se renombra. */
    @Column(name = "customer_name", nullable = false, length = 150)
    private String customerName;

    /** DNI (8 dígitos) o RUC (11). */
    @Column(name = "customer_doc", nullable = false, length = 11)
    private String customerDoc;

    // ─── Importes (IGV incluido) ────────────────────────────────────────────────

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal subtotal;

    /** Descuento por cupón. Siempre 0 hasta que exista el módulo de promociones. */
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal discount = BigDecimal.ZERO;

    @Column(name = "shipping_cost", nullable = false, precision = 12, scale = 2)
    private BigDecimal shippingCost;

    /** Desglosado hacia adentro del total, NO sumado encima. */
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal igv;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal total;

    // ─── Pago ───────────────────────────────────────────────────────────────────

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", length = 20)
    private MetodoPago paymentMethod;

    @Column(name = "paid_at")
    private Instant paidAt;

    // ─── Líneas ─────────────────────────────────────────────────────────────────

    /**
     * Cascade ALL + orphanRemoval: las líneas no existen sin su pedido, así que se
     * persisten junto con él en el mismo save del checkout.
     */
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("id ASC")
    private List<ItemOrden> items = new ArrayList<>();

    /** Mantiene los dos lados de la relación en sincronía al armar el pedido. */
    public void addItem(ItemOrden item) {
        items.add(item);
        item.setOrder(this);
    }
}
