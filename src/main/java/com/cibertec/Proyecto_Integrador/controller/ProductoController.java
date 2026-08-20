package com.cibertec.Proyecto_Integrador.controller;

import java.math.BigDecimal;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.cibertec.Proyecto_Integrador.dto.response.PageResponse;
import com.cibertec.Proyecto_Integrador.dto.response.ProductoResponse;
import com.cibertec.Proyecto_Integrador.service.ProductoService;

/**
 * Catálogo público de productos. {@code GET /api/products/**} es permitAll en SecurityConfig.
 *
 * <p>Sólo expone lectura y sólo de productos activos — el filtro {@code active=true} lo
 * aplica siempre ProductoServiceImpl. La escritura vive en AdminProductoController.
 */
@RestController
@RequestMapping("/api/products")
public class ProductoController {

    private final ProductoService productService;

    public ProductoController(ProductoService productService) {
        this.productService = productService;
    }

    /**
     * GET /api/products → 200 PageResponse&lt;ProductoResponse&gt;.
     * Filtros opcionales: name (like), categoryId, priceMin, priceMax.
     * Paginación por los params estándar de Spring Data: page (base cero), size, sort.
     * Los items NO traen la galería: para eso está el detalle.
     */
    @GetMapping
    public PageResponse<ProductoResponse> buscar(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) BigDecimal priceMin,
            @RequestParam(required = false) BigDecimal priceMax,
            Pageable pageable) {
        return productService.buscar(name, categoryId, priceMin, priceMax, pageable);
    }

    /** GET /api/products/{id} → 200 ProductoResponse con la galería. 404 si no existe o está inactivo. */
    @GetMapping("/{id}")
    public ProductoResponse buscarPorId(@PathVariable Long id) {
        return productService.buscarPorId(id);
    }
}
