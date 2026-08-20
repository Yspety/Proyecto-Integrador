package com.cibertec.Proyecto_Integrador.repository;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.cibertec.Proyecto_Integrador.dto.response.TopProductosReport.TopProductoRow;
import com.cibertec.Proyecto_Integrador.entity.ItemOrden;
import com.cibertec.Proyecto_Integrador.entity.enums.EstadoOrden;

public interface ItemOrdenRepository extends JpaRepository<ItemOrden, Long> {

    /**
     * Ranking de productos por unidades vendidas, agregado EN LA BASE.
     *
     * <p>Se agrega con SQL y no en Java a propósito: traer todas las líneas de todos los
     * pedidos del período para sumarlas en memoria escala pésimo, y acá el filtro y el
     * GROUP BY los resuelve el motor.
     *
     * <p>El rango es [from, to): límite superior EXCLUSIVO, para no perder las ventas
     * del último día por interpretar la fecha como medianoche.
     */
    @Query("""
            SELECT new com.cibertec.Proyecto_Integrador.dto.response.TopProductosReport$TopProductoRow(
                       p.id, p.sku, p.name, SUM(i.quantity), SUM(i.subtotal))
            FROM ItemOrden i
            JOIN i.order o
            JOIN i.product p
            WHERE o.status IN :statuses
              AND o.orderDate >= :from
              AND o.orderDate < :to
            GROUP BY p.id, p.sku, p.name
            ORDER BY SUM(i.quantity) DESC
            """)
    List<TopProductoRow> rankingVendidos(@Param("statuses") Collection<EstadoOrden> statuses,
                                         @Param("from") Instant from,
                                         @Param("to") Instant to,
                                         Pageable pageable);
}
