package com.cibertec.Proyecto_Integrador.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import com.cibertec.Proyecto_Integrador.entity.enums.Rol;

/** Alta de usuario por un ADMIN: el rol es elegible (CLIENTE o ADMIN). */
public record CreateUsuarioRequest(
        @NotBlank String name,
        @NotBlank @Email String email,
        @NotBlank String password,
        @NotNull Rol role) {
}