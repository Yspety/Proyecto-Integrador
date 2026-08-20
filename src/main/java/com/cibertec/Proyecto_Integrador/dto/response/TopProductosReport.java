package com.cibertec.Proyecto_Integrador.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/** Ranking de productos por unidades vendidas. Sólo pedidos pagados. */
public record TopProductosReport(
        LocalDate desde,
        LocalDate hasta,
        int limit,
        List<TopProductoRow> productos) {

    /**
     * Fila del ranking. {@code nombre} sale del CATÁLOGO (no del snapshot de la línea):
     * el reporte responde "cómo se vende este producto hoy", así que tiene que agrupar
     * bajo el nombre actual aunque el producto se haya renombrado desde la venta.
     */
    public record TopProductoRow(
            Long productId,
            String sku,
            String nombre,
            Long unidades,
            BigDecimal ingresos) {
    }
}
