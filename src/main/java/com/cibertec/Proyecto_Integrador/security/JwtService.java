package com.cibertec.Proyecto_Integrador.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.cibertec.Proyecto_Integrador.entity.Usuario;

/**
 * Genera y valida JWT HS256 (firma simétrica). El secreto se lee de config
 * (`app.jwt.secret`, override por env en prod). Stateless: no guarda nada.
 */
@Service
public class JwtService {

    private final SecretKey key;
    private final long expirationMs;

    public JwtService(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.expiration}") long expirationMs) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMs = expirationMs;
    }

    public String generateToken(Usuario user) {
        Date now = new Date();
        return Jwts.builder()
                .subject(user.getEmail())
                .claim("role", user.getRole().name())
                .issuedAt(now)
                .expiration(new Date(now.getTime() + expirationMs))
                .signWith(key)
                .compact();
    }

    public boolean isValid(String token) {
        try {
            parse(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false; // firma inválida, expirado, malformado o null
        }
    }

    public String extractEmail(String token) {
        return parse(token).getPayload().getSubject();
    }

    /** Rol del claim `role` ("ADMIN" | "CLIENTE"). Lo usa JwtAuthFilter para construir la authority. */
    public String extractRole(String token) {
        return parse(token).getPayload().get("role", String.class);
    }

    private Jws<Claims> parse(String token) {
        return Jwts.parser().verifyWith(key).build().parseSignedClaims(token);
    }
}