package com.cibertec.Proyecto_Integrador.repository;

import java.time.Instant;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.cibertec.Proyecto_Integrador.entity.MovimientoStock;

public interface MovimientoStockRepository extends JpaRepository<MovimientoStock, Long> {

    /** Kardex de un producto en orden cronológico. */
    List<MovimientoStock> findByProductIdOrderByCreatedAtAscIdAsc(Long productId);

    /** Kardex acotado a un rango [from, to). Lo consumirá el reporte de kardex. */
    List<MovimientoStock> findByProductIdAndCreatedAtBetweenOrderByCreatedAtAscIdAsc(
            Long productId, Instant from, Instant to);
}
