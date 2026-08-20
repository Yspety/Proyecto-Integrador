package com.cibertec.Proyecto_Integrador.dto.response;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

/**
 * Historial de stock de un producto. {@code stockActual} es el saldo de hoy
 * (Producto.stock), NO el saldo al final del rango: si se filtra por período, la suma
 * de los movimientos mostrados no tiene por qué cuadrar con él.
 */
public record KardexReport(
        Long productId,
        String sku,
        String nombre,
        int stockActual,
        LocalDate desde,
        LocalDate hasta,
        List<KardexMovimientoRow> movimientos) {

    /** {@code tipo} es ENTRADA o SALIDA; {@code cantidad} siempre positiva. */
    public record KardexMovimientoRow(
            Instant fecha,
            String tipo,
            int cantidad,
            String reason,
            String reference) {
    }
}
