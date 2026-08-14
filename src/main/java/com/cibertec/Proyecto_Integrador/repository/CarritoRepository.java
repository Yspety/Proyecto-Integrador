package com.cibertec.Proyecto_Integrador.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.cibertec.Proyecto_Integrador.entity.Carrito;
import com.cibertec.Proyecto_Integrador.entity.Usuario;

public interface CarritoRepository extends JpaRepository<Carrito, Long> {

    Optional<Carrito> findByUser(Usuario user);
}