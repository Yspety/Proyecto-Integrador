package com.cibertec.Proyecto_Integrador.service;

import java.time.LocalDate;
import com.cibertec.Proyecto_Integrador.dto.response.KardexReport;
import com.cibertec.Proyecto_Integrador.dto.response.TopProductosReport;
import com.cibertec.Proyecto_Integrador.dto.response.VentasPorPeriodoReport;

/** Reportes del panel de administración. Todas las lecturas son sólo de ADMIN. */
public interface ReporteService {

    /**
     * Ventas agrupadas por día o mes en el rango [desde, hasta], ambos INCLUSIVOS
     * en fecha de calendario de Lima. Sólo pedidos pagados.
     */
    VentasPorPeriodoReport ventasPorPeriodo(LocalDate desde, LocalDate hasta, String granularidad);

    /** Top {@code limit} productos por unidades vendidas en el rango. Sólo pedidos pagados. */
    TopProductosReport topProductos(LocalDate desde, LocalDate hasta, int limit);

    /**
     * Kardex de un producto. {@code desde} y {@code hasta} son opcionales: si vienen en
     * null se devuelve el historial completo.
     */
    KardexReport kardex(Long productId, LocalDate desde, LocalDate hasta);
}
