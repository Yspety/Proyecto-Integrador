package com.cibertec.Proyecto_Integrador.dto.request;

import jakarta.validation.constraints.NotNull;
import com.cibertec.Proyecto_Integrador.entity.enums.MetodoPago;

public record PaymentRequest(@NotNull MetodoPago method) {
}