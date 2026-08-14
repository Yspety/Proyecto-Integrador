package com.cibertec.Proyecto_Integrador.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record UpdateQuantityRequest(
        @NotNull @Min(1) Integer quantity) {
}