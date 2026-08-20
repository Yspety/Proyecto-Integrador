package com.cibertec.Proyecto_Integrador.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.cibertec.Proyecto_Integrador.dto.response.KardexReport;
import com.cibertec.Proyecto_Integrador.dto.response.KardexReport.KardexMovimientoRow;
import com.cibertec.Proyecto_Integrador.dto.response.TopProductosReport;
import com.cibertec.Proyecto_Integrador.dto.response.TopProductosReport.TopProductoRow;
import com.cibertec.Proyecto_Integrador.dto.response.VentasPorPeriodoReport;
import com.cibertec.Proyecto_Integrador.dto.response.VentasPorPeriodoReport.VentasPeriodoRow;
import com.cibertec.Proyecto_Integrador.entity.MovimientoStock;
import com.cibertec.Proyecto_Integrador.entity.Orden;
import com.cibertec.Proyecto_Integrador.entity.Producto;
import com.cibertec.Proyecto_Integrador.exception.ResourceNotFoundException;
import com.cibertec.Proyecto_Integrador.policy.EstadoOrdenPolicy;
import com.cibertec.Proyecto_Integrador.repository.ItemOrdenRepository;
import com.cibertec.Proyecto_Integrador.repository.MovimientoStockRepository;
import com.cibertec.Proyecto_Integrador.repository.OrdenRepository;
import com.cibertec.Proyecto_Integrador.repository.ProductoRepository;
import com.cibertec.Proyecto_Integrador.service.ReporteService;

@Service
public class ReporteServiceImpl implements ReporteService {

    /**
     * Todo se agrupa en hora de Lima, no en UTC.
     *
     * <p>No es un detalle cosmético: una venta de las 21:00 del 20/08 en Lima ocurre a
     * las 02:00 UTC del 21/08. Agrupando por UTC esa venta se contaría en el día
     * siguiente, y el reporte no coincidiría con lo que el negocio vio ese día.
     */
    private static final ZoneId LIMA = ZoneId.of("America/Lima");
    private static final DateTimeFormatter POR_DIA = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter POR_MES = DateTimeFormatter.ofPattern("yyyy-MM");

    private final OrdenRepository orderRepository;
    private final ItemOrdenRepository orderItemRepository;
    private final MovimientoStockRepository stockMovementRepository;
    private final ProductoRepository productRepository;

    public ReporteServiceImpl(OrdenRepository orderRepository,
                              ItemOrdenRepository orderItemRepository,
                              MovimientoStockRepository stockMovementRepository,
                              ProductoRepository productRepository) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.stockMovementRepository = stockMovementRepository;
        this.productRepository = productRepository;
    }

    // ─── ventas ─────────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public VentasPorPeriodoReport ventasPorPeriodo(LocalDate desde, LocalDate hasta, String granularidad) {
        boolean porMes = "mes".equalsIgnoreCase(granularidad);
        DateTimeFormatter fmt = porMes ? POR_MES : POR_DIA;

        List<Orden> orders = orderRepository
                .findByStatusInAndOrderDateGreaterThanEqualAndOrderDateLessThan(
                        EstadoOrdenPolicy.estadosPagados(), inicioDe(desde), finExclusivoDe(hasta));

        // Se agrupa en Java y no con funciones de fecha de SQL para no atarse a la
        // sintaxis de MySQL ni depender de la zona horaria configurada en el motor.
        // El volumen de un reporte acotado por fechas lo hace perfectamente viable.
        Map<String, Acumulador> buckets = new LinkedHashMap<>();
        BigDecimal totalFacturado = BigDecimal.ZERO;

        for (Orden order : orders) {
            String periodo = fmt.format(order.getOrderDate().atZone(LIMA));
            buckets.computeIfAbsent(periodo, k -> new Acumulador()).sumar(order.getTotal());
            totalFacturado = totalFacturado.add(order.getTotal());
        }

        // Buckets vacíos incluidos: un gráfico de línea con huecos miente sobre la
        // tendencia — un día sin ventas es un cero, no un punto que no existe.
        List<VentasPeriodoRow> filas = new ArrayList<>();
        for (String periodo : periodosDelRango(desde, hasta, porMes, fmt)) {
            Acumulador acc = buckets.getOrDefault(periodo, Acumulador.VACIO);
            filas.add(new VentasPeriodoRow(periodo, acc.ordenes, escala(acc.monto)));
        }

        long totalOrdenes = orders.size();
        BigDecimal ticket = totalOrdenes == 0
                ? BigDecimal.ZERO.setScale(2)
                : totalFacturado.divide(BigDecimal.valueOf(totalOrdenes), 2, RoundingMode.HALF_UP);

        return new VentasPorPeriodoReport(desde, hasta, porMes ? "mes" : "dia",
                totalOrdenes, escala(totalFacturado), ticket, filas);
    }

    // ─── top productos ──────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public TopProductosReport topProductos(LocalDate desde, LocalDate hasta, int limit) {
        int tope = Math.clamp(limit, 1, 100);   // un limit=100000 no puede tumbar el panel

        List<TopProductoRow> productos = orderItemRepository.rankingVendidos(
                EstadoOrdenPolicy.estadosPagados(),
                inicioDe(desde),
                finExclusivoDe(hasta),
                PageRequest.of(0, tope));

        return new TopProductosReport(desde, hasta, tope, productos);
    }

    // ─── kardex ─────────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public KardexReport kardex(Long productId, LocalDate desde, LocalDate hasta) {
        Producto product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado: " + productId));

        List<MovimientoStock> movimientos = (desde == null && hasta == null)
                ? stockMovementRepository.findByProductIdOrderByCreatedAtAscIdAsc(productId)
                : stockMovementRepository.findByProductIdAndCreatedAtBetweenOrderByCreatedAtAscIdAsc(
                        productId,
                        desde == null ? Instant.EPOCH : inicioDe(desde),
                        hasta == null ? Instant.now() : finExclusivoDe(hasta));

        List<KardexMovimientoRow> filas = movimientos.stream()
                .map(m -> new KardexMovimientoRow(
                        m.getCreatedAt(), m.getType().name(), m.getQuantity(), m.getReason(), m.getReference()))
                .toList();

        return new KardexReport(product.getId(), product.getSku(), product.getName(),
                product.getStock(), desde, hasta, filas);
    }

    // ─── helpers ────────────────────────────────────────────────────────────────

    /** Medianoche de Lima del día {@code date}. */
    private static Instant inicioDe(LocalDate date) {
        return date.atStartOfDay(LIMA).toInstant();
    }

    /**
     * Medianoche de Lima del día SIGUIENTE a {@code date}: el rango es [desde, hasta+1d),
     * de modo que {@code hasta} queda incluido completo, con sus 24 horas. Usar la
     * medianoche del propio {@code hasta} perdería todo lo vendido ese día.
     */
    private static Instant finExclusivoDe(LocalDate date) {
        return date.plusDays(1).atStartOfDay(LIMA).toInstant();
    }

    /** Todas las etiquetas de período del rango, incluidas las que no tuvieron ventas. */
    private static List<String> periodosDelRango(LocalDate desde, LocalDate hasta,
                                                 boolean porMes, DateTimeFormatter fmt) {
        List<String> periodos = new ArrayList<>();
        if (porMes) {
            LocalDate cursor = desde.withDayOfMonth(1);
            while (!cursor.isAfter(hasta)) {
                periodos.add(fmt.format(cursor));
                cursor = cursor.plusMonths(1);
            }
        } else {
            LocalDate cursor = desde;
            while (!cursor.isAfter(hasta)) {
                periodos.add(fmt.format(cursor));
                cursor = cursor.plusDays(1);
            }
        }
        return periodos;
    }

    private static BigDecimal escala(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP);
    }

    /** Acumulador mutable de un bucket. */
    private static final class Acumulador {
        private static final Acumulador VACIO = new Acumulador();

        private long ordenes;
        private BigDecimal monto = BigDecimal.ZERO;

        void sumar(BigDecimal total) {
            ordenes++;
            monto = monto.add(total);
        }
    }
}
