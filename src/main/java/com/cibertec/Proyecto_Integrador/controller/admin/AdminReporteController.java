package com.cibertec.Proyecto_Integrador.controller.admin;

import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.cibertec.Proyecto_Integrador.dto.response.KardexReport;
import com.cibertec.Proyecto_Integrador.dto.response.TopProductosReport;
import com.cibertec.Proyecto_Integrador.dto.response.VentasPorPeriodoReport;
import com.cibertec.Proyecto_Integrador.service.ReporteExportService;
import com.cibertec.Proyecto_Integrador.service.ReporteService;

/**
 * Reportes del dashboard — sólo ADMIN (/api/admin/** en SecurityConfig).
 *
 * <p>Las fechas viajan como {@code yyyy-MM-dd} y se interpretan como días de calendario
 * de LIMA, con ambos extremos incluidos. Todos los reportes de venta cuentan únicamente
 * pedidos pagados.
 */
@RestController
@RequestMapping("/api/admin/reports")
public class AdminReporteController {

    private static final String EXCEL =
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

    private final ReporteService reportService;
    private final ReporteExportService exportService;

    public AdminReporteController(ReporteService reportService, ReporteExportService exportService) {
        this.reportService = reportService;
        this.exportService = exportService;
    }

    /** GET /api/admin/reports/ventas?desde&hasta&granularidad=dia|mes → KPIs + serie temporal. */
    @GetMapping("/ventas")
    public VentasPorPeriodoReport ventas(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @RequestParam(defaultValue = "dia") String granularidad) {
        return reportService.ventasPorPeriodo(desde, hasta, granularidad);
    }

    /** GET /api/admin/reports/productos-vendidos?desde&hasta&limit → ranking por unidades. */
    @GetMapping("/productos-vendidos")
    public TopProductosReport productosVendidos(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @RequestParam(defaultValue = "10") int limit) {
        return reportService.topProductos(desde, hasta, limit);
    }

    /** GET /api/admin/reports/kardex?productId&desde&hasta → movimientos de stock. Rango opcional. */
    @GetMapping("/kardex")
    public KardexReport kardex(
            @RequestParam Long productId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        return reportService.kardex(productId, desde, hasta);
    }

    // ─── exports ────────────────────────────────────────────────────────────────
    // Mismos parámetros que los endpoints JSON: el archivo tiene que decir exactamente
    // lo mismo que la pantalla desde la que se lo bajó.

    @GetMapping(value = "/ventas/excel", produces = EXCEL)
    public ResponseEntity<byte[]> ventasExcel(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @RequestParam(defaultValue = "dia") String granularidad) {
        return archivo(exportService.ventasExcel(desde, hasta, granularidad),
                EXCEL, "ventas_" + desde + "_" + hasta + ".xlsx");
    }

    @GetMapping(value = "/ventas/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> ventasPdf(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @RequestParam(defaultValue = "dia") String granularidad) {
        return archivo(exportService.ventasPdf(desde, hasta, granularidad),
                MediaType.APPLICATION_PDF_VALUE, "ventas_" + desde + "_" + hasta + ".pdf");
    }

    @GetMapping(value = "/productos-vendidos/excel", produces = EXCEL)
    public ResponseEntity<byte[]> topProductosExcel(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @RequestParam(defaultValue = "10") int limit) {
        return archivo(exportService.topProductosExcel(desde, hasta, limit),
                EXCEL, "productos_" + desde + "_" + hasta + ".xlsx");
    }

    @GetMapping(value = "/productos-vendidos/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> topProductosPdf(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @RequestParam(defaultValue = "10") int limit) {
        return archivo(exportService.topProductosPdf(desde, hasta, limit),
                MediaType.APPLICATION_PDF_VALUE, "productos_" + desde + "_" + hasta + ".pdf");
    }

    @GetMapping(value = "/kardex/excel", produces = EXCEL)
    public ResponseEntity<byte[]> kardexExcel(
            @RequestParam Long productId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        return archivo(exportService.kardexExcel(productId, desde, hasta),
                EXCEL, "kardex_" + productId + ".xlsx");
    }

    @GetMapping(value = "/kardex/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> kardexPdf(
            @RequestParam Long productId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        return archivo(exportService.kardexPdf(productId, desde, hasta),
                MediaType.APPLICATION_PDF_VALUE, "kardex_" + productId + ".pdf");
    }

    private static ResponseEntity<byte[]> archivo(byte[] contenido, String contentType, String filename) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(contentType));
        headers.setContentDisposition(ContentDisposition.attachment().filename(filename).build());
        headers.setContentLength(contenido.length);
        return ResponseEntity.ok().headers(headers).body(contenido);
    }
}
