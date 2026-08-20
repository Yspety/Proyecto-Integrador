package com.cibertec.Proyecto_Integrador.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record ProductoRequest(
        @NotBlank @Size(max = 60) String sku,
        @NotBlank @Size(max = 150) String name,
        @Size(max = 2000) String description,
        @NotNull @DecimalMin("0.0") @Digits(integer = 12, fraction = 2) BigDecimal price,
        @NotNull @Min(0) Integer stock,
        /**
         * Punto de reposición. OPCIONAL a propósito: si viene null, en el alta se usa
         * {@code app.inventory.default-stock-min} y en la edición se conserva el valor
         * actual. Así los clientes que todavía no mandan el campo siguen funcionando
         * sin pisar la configuración con un null.
         */
        @Min(0) Integer stockMin,
        @Size(max = 500) String imageUrl,
        @NotNull Long categoryId) {
}