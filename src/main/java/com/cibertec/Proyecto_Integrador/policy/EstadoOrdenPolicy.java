package com.cibertec.Proyecto_Integrador.policy;

import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import com.cibertec.Proyecto_Integrador.entity.enums.EstadoOrden;

/**
 * Máquina de estados de un pedido. Única fuente de verdad sobre qué transición es legal.
 *
 * <pre>
 *   PENDIENTE ──▶ CONFIRMADA ──▶ ENVIADO ──▶ ENTREGADO
 *       │              │
 *       └──────┬───────┘
 *              ▼
 *          CANCELADA
 * </pre>
 *
 * <p>Reglas y por qué:
 * <ul>
 *   <li><b>No se salta pasos.</b> PENDIENTE no puede ir directo a ENVIADO: implicaría
 *       despachar algo que nadie pagó.</li>
 *   <li><b>Sólo se cancela antes de despachar.</b> Una vez ENVIADO el paquete está en
 *       la calle; eso es una devolución, no una cancelación, y es otro proceso.</li>
 *   <li><b>ENTREGADO y CANCELADA son terminales.</b> Un pedido cerrado no revive: si
 *       hiciera falta, se emite una nota de crédito, no se reabre el documento.</li>
 *   <li><b>No hay auto-transiciones.</b> Reintentar PUT status=ENVIADO sobre un pedido
 *       ya ENVIADO es un error del cliente, no un no-op silencioso.</li>
 * </ul>
 */
public final class EstadoOrdenPolicy {

    private static final Map<EstadoOrden, Set<EstadoOrden>> TRANSICIONES = Map.of(
            EstadoOrden.PENDIENTE,  EnumSet.of(EstadoOrden.CONFIRMADA, EstadoOrden.CANCELADA),
            EstadoOrden.CONFIRMADA, EnumSet.of(EstadoOrden.ENVIADO, EstadoOrden.CANCELADA),
            EstadoOrden.ENVIADO,    EnumSet.of(EstadoOrden.ENTREGADO),
            EstadoOrden.ENTREGADO,  EnumSet.noneOf(EstadoOrden.class),
            EstadoOrden.CANCELADA,  EnumSet.noneOf(EstadoOrden.class));

    private EstadoOrdenPolicy() {}

    /** ¿Se puede pasar de {@code from} a {@code to}? */
    public static boolean puedeTransicionar(EstadoOrden from, EstadoOrden to) {
        return TRANSICIONES.getOrDefault(from, Set.of()).contains(to);
    }

    /** Estados a los que se puede ir desde {@code from}. Vacío si es terminal. */
    public static Set<EstadoOrden> siguientes(EstadoOrden from) {
        return TRANSICIONES.getOrDefault(from, Set.of());
    }

    /**
     * ¿Este estado significa que el pedido ya fue pagado?
     *
     * <p>Determina si se puede emitir el comprobante. PENDIENTE todavía no pagó y
     * CANCELADA nunca se cobró: emitir un comprobante para cualquiera de los dos sería
     * declarar una venta que no ocurrió.
     */
    public static boolean estaPagado(EstadoOrden status) {
        return status == EstadoOrden.CONFIRMADA
                || status == EstadoOrden.ENVIADO
                || status == EstadoOrden.ENTREGADO;
    }

    /** ¿Esta transición devuelve la mercadería al stock? */
    public static boolean reponeStock(EstadoOrden from, EstadoOrden to) {
        return to == EstadoOrden.CANCELADA && from != EstadoOrden.CANCELADA;
    }

    /**
     * Estados que cuentan como venta concretada. Los reportes filtran por acá: un pedido
     * PENDIENTE todavía no es plata que entró y uno CANCELADO nunca lo fue, así que
     * incluirlos inflaría la facturación con ventas que no ocurrieron.
     */
    public static Set<EstadoOrden> estadosPagados() {
        return EnumSet.of(EstadoOrden.CONFIRMADA, EstadoOrden.ENVIADO, EstadoOrden.ENTREGADO);
    }
}
