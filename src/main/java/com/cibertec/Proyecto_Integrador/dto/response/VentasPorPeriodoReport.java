package com.cibertec.Proyecto_Integrador.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Ventas agrupadas por período. Los nombres de campo van en ESPAÑOL porque así los
 * declara el contrato del front (models/report.ts) — es la excepción a la regla de
 * "identificadores JSON en inglés" del resto de la API.
 *
 * <p>Sólo cuenta pedidos PAGADOS (CONFIRMADA/ENVIADO/ENTREGADO). Un pedido PENDIENTE
 * todavía no es plata que entró, y uno CANCELADO nunca lo fue: incluirlos inflaría la
 * facturación con ventas que no ocurrieron.
 */
public record VentasPorPeriodoReport(
        LocalDate desde,
        LocalDate hasta,
        String granularidad,
        long totalOrdenes,
        BigDecimal totalFacturado,
        BigDecimal ticketPromedio,
        List<VentasPeriodoRow> filas) {

    /** Un bucket del período: día (YYYY-MM-DD) o mes (YYYY-MM). */
    public record VentasPeriodoRow(String periodo, long ordenes, BigDecimal monto) {
    }
}
