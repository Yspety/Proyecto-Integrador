package com.cibertec.Proyecto_Integrador.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.math.BigDecimal;
import java.util.List;

public record ProductoResponse(
        Long id,
        String sku,
        String name,
        String description,
        BigDecimal price,
        int stock,
        /** Punto de reposición. {@code stock <= stockMin} dispara la alerta. */
        int stockMin,
        String imageUrl,
        boolean active,
        Long categoryId,
        String categoryName,
        @JsonInclude(JsonInclude.Include.NON_NULL) List<ImagenProductoResponse> images) {
}