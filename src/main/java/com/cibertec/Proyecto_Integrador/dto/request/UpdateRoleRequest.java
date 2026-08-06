package com.cibertec.Proyecto_Integrador.dto.request;

import jakarta.validation.constraints.NotNull;

import com.cibertec.Proyecto_Integrador.entity.enums.*;

public record UpdateRoleRequest(@NotNull Rol role) {
}