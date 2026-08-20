package com.cibertec.Proyecto_Integrador.controller;

import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.cibertec.Proyecto_Integrador.dto.response.CategoriaResponse;
import com.cibertec.Proyecto_Integrador.service.CategoriaService;

/**
 * Categorías públicas. Alimenta el filtro del catálogo y el selector de la home.
 * Sin paginar: el volumen de categorías es chico por naturaleza.
 */
@RestController
@RequestMapping("/api/categories")
public class CategoriaController {

    private final CategoriaService categoryService;

    public CategoriaController(CategoriaService categoryService) {
        this.categoryService = categoryService;
    }

    /** GET /api/categories → 200 List&lt;CategoriaResponse&gt; ordenado por nombre. */
    @GetMapping
    public List<CategoriaResponse> listar() {
        return categoryService.listar();
    }
}
