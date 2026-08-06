package com.cibertec.Proyecto_Integrador.service;

import java.util.List;
import com.cibertec.Proyecto_Integrador.dto.response.UsuarioResponse;
import com.cibertec.Proyecto_Integrador.dto.request.CreateUsuarioRequest;
import com.cibertec.Proyecto_Integrador.entity.enums.Rol;

/** Gestión de usuarios por un ADMIN. */
public interface UsuarioService {

    List<UsuarioResponse> listar();

    /** Crea un usuario con rol elegible (CLIENTE o ADMIN). */
    UsuarioResponse registrar(CreateUsuarioRequest request);

    /** Cambia el rol; bloquea si degrada al último ADMIN activo. */
    UsuarioResponse cambiarRol(Long id, Rol newRole);

    /** Activa/desactiva (baja lógica); bloquea si desactiva al último ADMIN activo. */
    UsuarioResponse cambiarEstado(Long id, boolean active);
}