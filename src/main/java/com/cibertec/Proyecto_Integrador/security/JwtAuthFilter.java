package com.cibertec.Proyecto_Integrador.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Traduce el header `Authorization: Bearer <jwt>` en una Authentication del SecurityContext.
 *
 * <p>Stateless: no consulta la base. El email sale del subject y el rol del claim `role`,
 * ambos ya firmados. El principal queda como el EMAIL (String), que es justo lo que los
 * services de dominio esperan como primer parámetro (obtenerCarrito(email), miOrden(email, id)).
 *
 * <p>Un token ausente o inválido no lanza: deja la request como anónima y que
 * SecurityConfig decida si el endpoint la admite (401 si no).
 *
 * <p>NO es un bean a propósito: Spring Boot auto-registra cualquier bean de tipo
 * Filter en la cadena del servlet container, y correría además de (y antes de) la
 * cadena de Security. SecurityConfig lo instancia a mano para que viva sólo ahí.
 */
public class JwtAuthFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtService jwtService;

    public JwtAuthFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String token = extractToken(request);

        if (token != null
                && jwtService.isValid(token)
                && SecurityContextHolder.getContext().getAuthentication() == null) {

            String email = jwtService.extractEmail(token);
            String role = jwtService.extractRole(token);

            if (email != null && role != null) {
                // hasRole("ADMIN") compara contra "ROLE_ADMIN": el prefijo va acá.
                var authority = new SimpleGrantedAuthority("ROLE_" + role);
                var authentication = new UsernamePasswordAuthenticationToken(
                        email, null, List.of(authority));
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }

        filterChain.doFilter(request, response);
    }

    private String extractToken(HttpServletRequest request) {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header == null || !header.startsWith(BEARER_PREFIX)) {
            return null;
        }
        String token = header.substring(BEARER_PREFIX.length()).trim();
        return token.isEmpty() ? null : token;
    }
}
