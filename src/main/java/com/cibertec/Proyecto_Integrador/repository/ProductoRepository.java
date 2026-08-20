package com.cibertec.Proyecto_Integrador.repository;

import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.cibertec.Proyecto_Integrador.entity.Producto;

public interface ProductoRepository extends JpaRepository<Producto, Long>,
        JpaSpecificationExecutor<Producto> {

    boolean existsBySku(String sku);

    boolean existsBySkuAndIdNot(String sku, Long id);

    boolean existsByCategoryId(Long categoryId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Producto p WHERE p.id = :id")
    Optional<Producto> findByIdWithLock(@Param("id") Long id);

    /**
     * Productos activos que llegaron a su punto de reposición, los más críticos primero.
     *
     * <p>Sólo activos: un producto dado de baja no se repone, y meterlo en la alerta
     * sería ruido que hace que se deje de mirar la lista.
     *
     * <p>Ordena por faltante DESC y desempata por stock ASC, para que un producto en
     * cero quede arriba de otro al que le falta lo mismo pero todavía tiene unidades.
     */
    @Query("""
            SELECT p FROM Producto p
            WHERE p.active = true AND p.stock <= p.stockMin
            ORDER BY (p.stockMin - p.stock) DESC, p.stock ASC
            """)
    List<Producto> findPorReponer();
}