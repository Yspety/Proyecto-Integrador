package com.cibertec.Proyecto_Integrador.service.impl;

import java.time.Instant;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.cibertec.Proyecto_Integrador.dto.request.LoginRequest;
import com.cibertec.Proyecto_Integrador.dto.request.RegisterRequest;
import com.cibertec.Proyecto_Integrador.dto.response.AuthResponse;
import com.cibertec.Proyecto_Integrador.dto.response.UsuarioResponse;
import com.cibertec.Proyecto_Integrador.exception.DuplicateEmailException;
import com.cibertec.Proyecto_Integrador.exception.InvalidCredentialsException;
import com.cibertec.Proyecto_Integrador.mapper.UsuarioMapper;
import com.cibertec.Proyecto_Integrador.entity.Usuario;
import com.cibertec.Proyecto_Integrador.entity.enums.Rol;
import com.cibertec.Proyecto_Integrador.repository.UsuarioRepository;
import com.cibertec.Proyecto_Integrador.security.JwtService;
import com.cibertec.Proyecto_Integrador.service.AuthService;

@Service
public class AuthServiceImpl implements AuthService {

    private final UsuarioRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final UsuarioMapper userMapper;
    private final long expirationMs;

    public AuthServiceImpl(UsuarioRepository userRepository,
                           PasswordEncoder passwordEncoder,
                           JwtService jwtService,
                           UsuarioMapper userMapper,
                           @Value("${app.jwt.expiration}") long expirationMs) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.userMapper = userMapper;
        this.expirationMs = expirationMs;
    }

    @Override
    @Transactional
    public UsuarioResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new DuplicateEmailException("El email ya está registrado");
        }
        Usuario user = new Usuario();
        user.setName(request.name());
        user.setEmail(request.email());
        user.setPassword(passwordEncoder.encode(request.password())); // hash, nunca plano
        user.setRole(Rol.CLIENTE);                                   // rol fijo desde el registro público
        user.setActive(true);
        user.setCreatedAt(Instant.now());
        return userMapper.toResponse(userRepository.save(user));
    }

    @Override
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        // Mismo error para email inexistente / password incorrecto / inactivo:
        // no filtrar qué emails existen.
        Usuario user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new InvalidCredentialsException("Credenciales inválidas"));
        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new InvalidCredentialsException("Credenciales inválidas");
        }
        if (!user.isActive()) {
            throw new InvalidCredentialsException("Credenciales inválidas");
        }
        return new AuthResponse(jwtService.generateToken(user), "Bearer", expirationMs / 1000);
    }
}