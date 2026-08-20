package com.cibertec.Proyecto_Integrador.config;

import jakarta.servlet.DispatcherType;
import java.util.Arrays;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import com.cibertec.Proyecto_Integrador.security.JwtAuthFilter;
import com.cibertec.Proyecto_Integrador.security.JwtService;

/**
 * Seguridad de la API: stateless, sin sesión ni formulario de login.
 *
 * <p>Reglas de acceso:
 * <ul>
 *   <li>{@code /api/auth/**} — público (login y registro).</li>
 *   <li>{@code GET /api/products/**}, {@code GET /api/categories/**} — catálogo público.</li>
 *   <li>{@code /api/uploads/**} — imágenes servidas públicamente.</li>
 *   <li>{@code /api/admin/**} — sólo ROLE_ADMIN.</li>
 *   <li>el resto — autenticado (carrito, pedidos, reseñas).</li>
 * </ul>
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /** BCrypt: lo inyectan AuthServiceImpl y UsuarioServiceImpl para hashear passwords. */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtService jwtService) throws Exception {
        http
            // API stateless con JWT: no hay sesión ni formulario, así que CSRF no aplica.
            .csrf(csrf -> csrf.disable())
            .cors(cors -> {})
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                    // Desde Spring Security 6 la cadena also corre en los dispatch de ERROR.
                    // Sin esto, cualquier excepción no manejada se re-despacha a /error como
                    // anónima y sale como 401 — o sea, un 500 real disfrazado de "no estás
                    // logueado". Se depura durante horas el token equivocado.
                    .dispatcherTypeMatchers(DispatcherType.ERROR, DispatcherType.ASYNC).permitAll()
                    .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                    .requestMatchers("/api/auth/**").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/products/**").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/categories/**").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/uploads/**").permitAll()
                    .requestMatchers("/api/admin/**").hasRole("ADMIN")
                    .anyRequest().authenticated())
            // Sin esto Security devuelve 403 al anónimo; el front necesita distinguir
            // "no estás logueado" (401) de "no te alcanza el rol" (403).
            .exceptionHandling(ex -> ex
                    .authenticationEntryPoint((req, res, e) ->
                            writeError(res, HttpStatus.UNAUTHORIZED, "No autenticado"))
                    .accessDeniedHandler((req, res, e) ->
                            writeError(res, HttpStatus.FORBIDDEN, "Acceso denegado")))
            .addFilterBefore(new JwtAuthFilter(jwtService), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /** Origen del dev server de Vite. allowCredentials=false: el token va por header, no por cookie. */
    @Bean
    public CorsConfigurationSource corsConfigurationSource(
            @Value("${app.cors.allowed-origins:http://localhost:5173}") String allowedOrigins) {

        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(Arrays.stream(allowedOrigins.split(",")).map(String::trim).toList());
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        // Necesario para que el front pueda leer el filename del PDF del comprobante.
        config.setExposedHeaders(List.of("Content-Disposition"));
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    /** Cuerpo de error con la misma forma que lee apiErrorMessage() en el front: { "error": "..." }. */
    private static void writeError(jakarta.servlet.http.HttpServletResponse response,
                                   HttpStatus status,
                                   String message) throws java.io.IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write("{\"error\":\"" + message + "\"}");
    }
}
