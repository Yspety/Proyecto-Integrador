package com.cibertec.Proyecto_Integrador.repository;


import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.cibertec.Proyecto_Integrador.entity.Usuario;
import com.cibertec.Proyecto_Integrador.entity.enums.Rol;
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByEmail(String email);

    boolean existsByEmail(String email);

    long countByRoleAndActiveTrue(Rol role);
}