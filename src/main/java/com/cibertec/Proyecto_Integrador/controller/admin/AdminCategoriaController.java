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
import com.cibertec.Proyecto_Integrador.dto.request.CategoriaRequest;
import com.cibertec.Proyecto_Integrador.dto.response.CategoriaResponse;
import com.cibertec.Proyecto_Integrador.service.CategoriaService;

/**
 * Escritura de categorías — sólo ADMIN. El listado se lee del público /api/categories.
 */
@RestController
@RequestMapping("/api/admin/categories")
public class AdminCategoriaController {

    private final CategoriaService categoryService;

    public AdminCategoriaController(CategoriaService categoryService) {
        this.categoryService = categoryService;
    }

    /** POST /api/admin/categories → 201 CategoriaResponse. 409 si el nombre ya existe. */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CategoriaResponse registrar(@Valid @RequestBody CategoriaRequest request) {
        return categoryService.registrar(request);
    }

    /** PUT /api/admin/categories/{id} → 200 CategoriaResponse. 404 si no existe, 409 si el nombre choca. */
    @PutMapping("/{id}")
    public CategoriaResponse actualizar(@PathVariable Long id,
                                        @Valid @RequestBody CategoriaRequest request) {
        return categoryService.actualizar(id, request);
    }

    /** DELETE /api/admin/categories/{id} → 204. 409 si todavía tiene productos asociados. */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id) {
        categoryService.eliminar(id);
    }
}
