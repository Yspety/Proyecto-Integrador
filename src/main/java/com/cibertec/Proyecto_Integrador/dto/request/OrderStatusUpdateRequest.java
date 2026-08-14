package com.cibertec.Proyecto_Integrador.dto.request;



import jakarta.validation.constraints.NotNull;
import com.cibertec.Proyecto_Integrador.entity.enums.EstadoOrden;

public record OrderStatusUpdateRequest(@NotNull EstadoOrden status) {
}