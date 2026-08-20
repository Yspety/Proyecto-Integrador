package com.cibertec.Proyecto_Integrador.dto.response;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * Respuesta de pedido. Incluye el desglose del comprobante: el precio del catálogo
 * YA trae IGV incluido, por eso `igv` se desglosa hacia adentro del total
 * (base = total − igv). `total = subtotal − discount + shippingCost`. Boleta y factura
 * devuelven el mismo total; sólo cambia si el front muestra el desglose.
 *
 * <p>`discount` existe porque el contrato del front (models/order.ts) lo declara como
 * campo obligatorio. Hasta que exista el módulo de promociones siempre vale 0 —
 * mandarlo en 0 es mejor que omitirlo y que el front reciba undefined.
 */
public record OrdenResponse(
        Long id,
        Long userId,
        Instant orderDate,
        String status,
        String documentType,
        String customerName,
        String customerDoc,
        BigDecimal subtotal,
        BigDecimal discount,
        BigDecimal shippingCost,
        BigDecimal igv,
        BigDecimal total,
        List<ItemOrdenResponse> items) {
}