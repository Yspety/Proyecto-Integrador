package com.cibertec.Proyecto_Integrador.dto.response;

import java.time.Instant;
import com.cibertec.Proyecto_Integrador.entity.enums.*;
public record UsuarioResponse(
        Long id, String name, String email, Rol role, boolean active, Instant createdAt) {
}