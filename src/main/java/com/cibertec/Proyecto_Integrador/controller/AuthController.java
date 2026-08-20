package com.cibertec.Proyecto_Integrador.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import com.cibertec.Proyecto_Integrador.dto.request.LoginRequest;
import com.cibertec.Proyecto_Integrador.dto.request.RegisterRequest;
import com.cibertec.Proyecto_Integrador.dto.response.AuthResponse;
import com.cibertec.Proyecto_Integrador.dto.response.UsuarioResponse;
import com.cibertec.Proyecto_Integrador.service.AuthService;

/**
 * Autenticación pública. {@code /api/auth/**} está permitido sin token en SecurityConfig.
 *
 * <p>El registro NO devuelve token: crea el usuario y el front encadena un login
 * (ver AuthContext.register). Mantener esa separación deja el alta por ADMIN
 * (/api/admin/users) y el alta pública compartiendo la misma forma de respuesta.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /** POST /api/auth/register → 201 UsuarioResponse. Rol fijo CLIENTE. 409 si el email ya existe. */
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public UsuarioResponse register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    /** POST /api/auth/login → 200 AuthResponse. 401 si las credenciales fallan o el usuario está inactivo. */
    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }
}
