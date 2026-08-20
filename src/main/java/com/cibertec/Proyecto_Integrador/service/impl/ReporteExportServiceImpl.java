package com.cibertec.Proyecto_Integrador.service.impl;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.springframework.stereotype.Service;
import com.cibertec.Proyecto_Integrador.dto.response.KardexReport;
import com.cibertec.Proyecto_Integrador.dto.response.TopProductosReport;
import com.cibertec.Proyecto_Integrador.dto.response.VentasPorPeriodoReport;
import com.cibertec.Proyecto_Integrador.service.ReporteExportService;
import com.cibertec.Proyecto_Integrador.service.ReporteService;
import com.cibertec.Proyecto_Integrador.service.export.ExcelWriter;
import com.cibertec.Proyecto_Integrador.service.export.PdfWriter;
import com.cibertec.Proyecto_Integrador.service.export.TablaReporte;
import com.cibertec.Proyecto_Integrador.service.export.TablaReporte.Kpi;

/**
 * Arma la {@link TablaReporte} de cada reporte y la manda al renderizador que toque.
 *
 * <p>Tres constructores de tabla × dos renderizadores = seis exports, sin duplicar
 * ni la consulta ni el formato.
 */
@Service
public class ReporteExportServiceImpl implements ReporteExportService {

    private static final ZoneId LIMA = ZoneId.of("America/Lima");
    private static final DateTimeFormatter FECHA_HORA =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm").withZone(LIMA);

    private final ReporteService reportService;
    private final ExcelWriter excelWriter;
    private final PdfWriter pdfWriter;

    public ReporteExportServiceImpl(ReporteService reportService,
                                    ExcelWriter excelWriter,
                                    PdfWriter pdfWriter) {
        this.reportService = reportService;
        this.excelWriter = excelWriter;
        this.pdfWriter = pdfWriter;
    }

    // ─── ventas ─────────────────────────────────────────────────────────────────

    @Override
    public byte[] ventasExcel(LocalDate desde, LocalDate hasta, String granularidad) {
        return excelWriter.escribir(tablaVentas(desde, hasta, granularidad));
    }

    @Override
    public byte[] ventasPdf(LocalDate desde, LocalDate hasta, String granularidad) {
        return pdfWriter.escribir(tablaVentas(desde, hasta, granularidad));
    }

    /**
     * El reporte JSON devuelve TODOS los períodos del rango, incluidos los que no
     * vendieron nada: el gráfico de línea los necesita, porque unir dos puntos salteando
     * los días muertos dibuja una tendencia que no existió.
     *
     * <p>Una tabla no tiene ese problema — una fila ausente no insinúa nada — y treinta
     * filas en cero sólo entierran los días que importan. Así que acá se filtran, y el
     * subtítulo lo dice para que nadie compare el Excel con el gráfico y crea que se
     * perdieron datos.
     */
    private TablaReporte tablaVentas(LocalDate desde, LocalDate hasta, String granularidad) {
        VentasPorPeriodoReport r = reportService.ventasPorPeriodo(desde, hasta, granularidad);

        List<List<String>> filas = r.filas().stream()
                .filter(f -> f.ordenes() > 0)
                .map(f -> List.of(f.periodo(), String.valueOf(f.ordenes()), f.monto().toPlainString()))
                .toList();

        boolean seOmitieron = filas.size() < r.filas().size();
        String subtitulo = periodo(desde, hasta) + " · sólo pedidos pagados"
                + (seOmitieron ? " · se omiten los períodos sin ventas" : "");

        return new TablaReporte(
                "Ventas por " + ("mes".equals(r.granularidad()) ? "mes" : "día"),
                subtitulo,
                List.of("Período", "Órdenes", "Monto (S/)"),
                List.of(false, true, true),
                filas,
                List.of(new Kpi("Total facturado", "S/ " + r.totalFacturado().toPlainString()),
                        new Kpi("Órdenes", String.valueOf(r.totalOrdenes())),
                        new Kpi("Ticket promedio", "S/ " + r.ticketPromedio().toPlainString())));
    }

    // ─── productos vendidos ─────────────────────────────────────────────────────

    @Override
    public byte[] topProductosExcel(LocalDate desde, LocalDate hasta, int limit) {
        return excelWriter.escribir(tablaTopProductos(desde, hasta, limit));
    }

    @Override
    public byte[] topProductosPdf(LocalDate desde, LocalDate hasta, int limit) {
        return pdfWriter.escribir(tablaTopProductos(desde, hasta, limit));
    }

    private TablaReporte tablaTopProductos(LocalDate desde, LocalDate hasta, int limit) {
        TopProductosReport r = reportService.topProductos(desde, hasta, limit);

        List<List<String>> filas = r.productos().stream()
                .map(p -> List.of(p.sku(), p.nombre(),
                        String.valueOf(p.unidades()), p.ingresos().toPlainString()))
                .toList();

        return new TablaReporte(
                "Productos más vendidos",
                periodo(desde, hasta) + " · top " + r.limit() + " · sólo pedidos pagados",
                List.of("SKU", "Producto", "Unidades", "Ingresos (S/)"),
                List.of(false, false, true, true),
                filas,
                List.of());
    }

    // ─── kardex ─────────────────────────────────────────────────────────────────

    @Override
    public byte[] kardexExcel(Long productId, LocalDate desde, LocalDate hasta) {
        return excelWriter.escribir(tablaKardex(productId, desde, hasta));
    }

    @Override
    public byte[] kardexPdf(Long productId, LocalDate desde, LocalDate hasta) {
        return pdfWriter.escribir(tablaKardex(productId, desde, hasta));
    }

    private TablaReporte tablaKardex(Long productId, LocalDate desde, LocalDate hasta) {
        KardexReport r = reportService.kardex(productId, desde, hasta);

        List<List<String>> filas = r.movimientos().stream()
                .map(m -> List.of(FECHA_HORA.format(m.fecha()), m.tipo(),
                        String.valueOf(m.cantidad()), m.reason(), m.reference()))
                .toList();

        return new TablaReporte(
                "Kardex · " + r.nombre(),
                "SKU " + r.sku() + " · " + periodo(desde, hasta) + " · stock actual: " + r.stockActual(),
                List.of("Fecha", "Tipo", "Cantidad", "Motivo", "Referencia"),
                List.of(false, false, true, false, false),
                filas,
                List.of(new Kpi("Stock actual", String.valueOf(r.stockActual())),
                        new Kpi("Movimientos", String.valueOf(r.movimientos().size()))));
    }

    // ─── helpers ────────────────────────────────────────────────────────────────

    private static String periodo(LocalDate desde, LocalDate hasta) {
        if (desde == null && hasta == null) {
            return "Histórico completo";
        }
        return "Del " + (desde != null ? desde : "inicio") + " al " + (hasta != null ? hasta : "hoy");
    }
}
