package com.cibertec.Proyecto_Integrador.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.cibertec.Proyecto_Integrador.entity.ImagenProducto;

public interface ImagenProductoRepository extends JpaRepository<ImagenProducto, Long> {

    long countByProductId(Long productId);

    List<ImagenProducto> findByProductId(Long productId);

    Optional<ImagenProducto> findByProductIdAndIsCoverTrue(Long productId);

    /**
     * Promotion candidate after cover deletion: first non-deleted image by
     * lowest display_order (then id as tiebreaker), excluding the image being deleted.
     */
    Optional<ImagenProducto> findFirstByProductIdAndIdNotOrderByDisplayOrderAscIdAsc(
            Long productId, Long excludedId);
}