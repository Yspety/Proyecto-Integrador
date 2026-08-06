package com.cibertec.Proyecto_Integrador.dto.request;

import jakarta.validation.constraints.NotNull;

public record UpdateStatusRequest(@NotNull Boolean active) {
}