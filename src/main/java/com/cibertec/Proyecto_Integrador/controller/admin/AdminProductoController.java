package com.cibertec.Proyecto_Integrador.controller.admin;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import com.cibertec.Proyecto_Integrador.dto.request.ProductoRequest;
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
