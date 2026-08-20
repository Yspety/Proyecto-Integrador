package com.cibertec.Proyecto_Integrador.service;

import java.time.LocalDate;

/**
 * Exporta los mismos reportes que sirve {@link ReporteService}, en Excel o PDF.
 *
 * <p>Reusa ReporteService para los datos: el export NO puede tener su propia consulta,
 * o el Excel terminaría mostrando números distintos a los del dashboard.
 */
public interface ReporteExportService {

    byte[] ventasExcel(LocalDate desde, LocalDate hasta, String granularidad);

    byte[] ventasPdf(LocalDate desde, LocalDate hasta, String granularidad);

    byte[] topProductosExcel(LocalDate desde, LocalDate hasta, int limit);

    byte[] topProductosPdf(LocalDate desde, LocalDate hasta, int limit);

    byte[] kardexExcel(Long productId, LocalDate desde, LocalDate hasta);

    byte[] kardexPdf(Long productId, LocalDate desde, LocalDate hasta);
}
