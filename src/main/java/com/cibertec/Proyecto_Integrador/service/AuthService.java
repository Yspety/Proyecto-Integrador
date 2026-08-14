package com.cibertec.Proyecto_Integrador.service;

import com.cibertec.Proyecto_Integrador.dto.request.LoginRequest;
import com.cibertec.Proyecto_Integrador.dto.request.RegisterRequest;
import com.cibertec.Proyecto_Integrador.dto.response.AuthResponse;
import com.cibertec.Proyecto_Integrador.dto.response.UsuarioResponse;

public interface AuthService {

    /** Registra un CLIENTE (password hasheado, email único). */
    UsuarioResponse register(RegisterRequest request);

    /** Valida credenciales + que el usuario esté activo y emite el JWT. */
    AuthResponse login(LoginRequest request);
}