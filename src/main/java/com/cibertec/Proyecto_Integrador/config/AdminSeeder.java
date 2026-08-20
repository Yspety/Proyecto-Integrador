package com.cibertec.Proyecto_Integrador.config;

import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import com.cibertec.Proyecto_Integrador.entity.Usuario;
import com.cibertec.Proyecto_Integrador.entity.enums.Rol;
import com.cibertec.Proyecto_Integrador.repository.UsuarioRepository;

/**
 * Crea el primer ADMIN cuando la base no tiene ninguno.
 *
 * <p>Existe para romper un huevo-gallina: el registro público fuerza {@link Rol#CLIENTE}
 * y el alta de administradores vive en {@code /api/admin/users}, que exige ROLE_ADMIN.
 * Sin esto no hay forma de entrar al panel por primera vez.
 *
 * <p>Tres reglas que lo hacen seguro de dejar encendido:
 * <ol>
 *   <li><b>Idempotente</b>: sólo actúa si no existe NINGÚN admin activo. Correrlo mil
 *       veces crea un usuario una sola vez, y si degradás al admin real vuelve a haber
 *       una puerta de entrada en vez de quedar afuera para siempre.</li>
 *   <li><b>Configurable</b>: email, nombre y password salen de {@code app.seed.admin.*},
 *       overrideables por env var. No hay credenciales incrustadas en el código.</li>
 *   <li><b>Ruidoso</b>: avisa por WARN con las credenciales que usó, para que nadie
 *       se olvide de que ese usuario existe.</li>
 * </ol>
 *
 * <p>Para apagarlo: {@code app.seed.admin.enabled=false}.
 */
@Component
@ConditionalOnProperty(name = "app.seed.admin.enabled", havingValue = "true", matchIfMissing = true)
public class AdminSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminSeeder.class);

    private final UsuarioRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final String email;
    private final String name;
    private final String password;

    public AdminSeeder(UsuarioRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       @Value("${app.seed.admin.email}") String email,
                       @Value("${app.seed.admin.name}") String name,
                       @Value("${app.seed.admin.password}") String password) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.email = email;
        this.name = name;
        this.password = password;
    }

    @Override
    public void run(String... args) {
        if (userRepository.countByRoleAndActiveTrue(Rol.ADMIN) > 0) {
            return; // ya hay un admin: no tocar nada
        }

        // El email podría existir como CLIENTE. Promoverlo en vez de fallar por el unique
        // deja al seeder utilizable también cuando ya te registraste con ese correo.
        Usuario admin = userRepository.findByEmail(email).orElseGet(Usuario::new);
        boolean isPromotion = admin.getId() != null;

        if (!isPromotion) {
            admin.setName(name);
            admin.setEmail(email);
            admin.setPassword(passwordEncoder.encode(password));
            admin.setCreatedAt(Instant.now());
        }
        admin.setRole(Rol.ADMIN);
        admin.setActive(true);
        userRepository.save(admin);

        log.warn("""

                ┌──────────────────────────────────────────────────────────────┐
                │  NO HABÍA NINGÚN ADMIN — se {}  │
                └──────────────────────────────────────────────────────────────┘
                  email    : {}
                  password : {}

                  CAMBIÁ ESTA CLAVE. Definí app.seed.admin.password (o la env var
                  ADMIN_PASSWORD), o apagá el seeder con app.seed.admin.enabled=false
                  una vez que tengas tu administrador real.
                """,
                isPromotion ? "promovió el usuario existente" : "creó uno nuevo               ",
                email,
                isPromotion ? "(la que ya tenía ese usuario)" : password);
    }
}
