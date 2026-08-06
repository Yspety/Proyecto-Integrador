package com.cibertec.Proyecto_Integrador.mapper;

import org.springframework.stereotype.Component;

import com.cibertec.Proyecto_Integrador.dto.response.UsuarioResponse;
import com.cibertec.Proyecto_Integrador.entity.Usuario;

/** Traduce la entidad Usuario a su DTO de salida (sin exponer el password). */
@Component
public class UsuarioMapper {

    public UsuarioResponse toResponse(Usuario user) {
        return new UsuarioResponse(
                user.getId(), user.getName(), user.getEmail(),
                user.getRole(), user.isActive(), user.getCreatedAt());
    }
}