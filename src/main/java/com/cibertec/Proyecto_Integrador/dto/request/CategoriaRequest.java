package com.cibertec.Proyecto_Integrador.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CategoriaRequest(
        @NotBlank @Size(max = 80) String name,
        @Size(max = 255) String description) {
}