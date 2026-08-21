package com.cibertec.Proyecto_Integrador.controller.admin;

import jakarta.validation.Valid;
import java.math.BigDecimal;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import com.cibertec.Proyecto_Integrador.dto.request.ProductoRequest;
import com.cibertec.Proyecto_Integrador.dto.request.UpdateStatusRequest;
import com.cibertec.Proyecto_Integrador.dto.response.PageResponse;
import com.cibertec.Proyecto_Integrador.dto.response.ProductoResponse;
import com.cibertec.Proyecto_Integrador.service.ProductoService;

/**
 * Alta, edición y baja de productos — sólo ADMIN (/api/admin/** en SecurityConfig).
 *
 * <p>La LECTURA no vive acá: el panel consume el mismo {@code GET /api/products}
 * público, así que no hay dos búsquedas que mantener sincronizadas.
 */
@RestController
@RequestMapping("/api/admin/products")
public class AdminProductoController {

    private final ProductoService productService;

    public AdminProductoController(ProductoService productService) {
        this.productService = productService;
    }

    /**
     * GET /api/admin/products → 200 PageResponse&lt;ProductoResponse&gt;, INCLUYENDO los dados de baja.
     * Mismos filtros que el listado público más {@code active} (true/false/omitido = todos).
     */
    @GetMapping
    public PageResponse<ProductoResponse> buscar(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) BigDecimal priceMin,
            @RequestParam(required = false) BigDecimal priceMax,
            @RequestParam(required = false) Boolean active,
            Pageable pageable) {
        return productService.buscarAdmin(name, categoryId, priceMin, priceMax, active, pageable);
    }

    /** PATCH /api/admin/products/{id}/status → 200. Reactiva un producto eliminado, o lo da de baja. */
    @PatchMapping("/{id}/status")
    public ProductoResponse cambiarEstado(@PathVariable Long id,
                                          @Valid @RequestBody UpdateStatusRequest request) {
        return productService.cambiarEstado(id, request.active());
    }

    /** POST /api/admin/products → 201 ProductoResponse. 409 si el SKU ya existe, 404 si la categoría no. */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProductoResponse registrar(@Valid @RequestBody ProductoRequest request) {
        return productService.registrar(request);
    }

    /**
     * PUT /api/admin/products/{id} → 200 ProductoResponse.
     * OJO: el {@code stock} del body se IGNORA — es read-only después del alta.
     */
    @PutMapping("/{id}")
    public ProductoResponse actualizar(@PathVariable Long id,
                                       @Valid @RequestBody ProductoRequest request) {
        return productService.actualizar(id, request);
    }

    /** DELETE /api/admin/products/{id} → 204. Soft delete: marca active=false, no borra la fila. */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id) {
        productService.eliminar(id);
    }
}
