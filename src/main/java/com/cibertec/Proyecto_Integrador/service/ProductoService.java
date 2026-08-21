package com.cibertec.Proyecto_Integrador.service;

import java.math.BigDecimal;
import org.springframework.data.domain.Pageable;
import com.cibertec.Proyecto_Integrador.dto.request.ProductoRequest;
import com.cibertec.Proyecto_Integrador.dto.response.PageResponse;
import com.cibertec.Proyecto_Integrador.dto.response.ProductoResponse;

/** Operaciones de catálogo para productos. */
public interface ProductoService {

    /**
     * Búsqueda pública con filtros opcionales. Siempre filtra active=true
     * en la ruta pública; el admin puede omitir ese filtro si se expone otra firma.
     */
    PageResponse<ProductoResponse> buscar(String name, Long categoryId,
                                         BigDecimal priceMin, BigDecimal priceMax,
                                         Pageable pageable);

    /**
     * Búsqueda del panel admin: incluye productos dados de baja.
     * {@code active} null lista todos; true/false filtra.
     */
    PageResponse<ProductoResponse> buscarAdmin(String name, Long categoryId,
                                               BigDecimal priceMin, BigDecimal priceMax,
                                               Boolean active, Pageable pageable);

    /** Da de baja (false) o reactiva (true) un producto. Contraparte del soft-delete. */
    ProductoResponse cambiarEstado(Long id, boolean active);

    /** Retorna el producto activo o lanza ResourceNotFoundException (404). */
    ProductoResponse buscarPorId(Long id);

    /** Crea un producto. SKU único; categoría debe existir; stock = bootstrap only. */
    ProductoResponse registrar(ProductoRequest request);

    /**
     * Actualiza un producto. SKU único excluyendo propio id.
     * IMPORTANTE: el campo stock del request es ignorado — stock es READ-ONLY post-creación.
     */
    ProductoResponse actualizar(Long id, ProductoRequest request);

    /** Soft-delete: establece active=false. NO elimina la fila. */
    void eliminar(Long id);
}