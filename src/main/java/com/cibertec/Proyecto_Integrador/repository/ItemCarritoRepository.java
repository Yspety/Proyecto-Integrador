package com.cibertec.Proyecto_Integrador.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.cibertec.Proyecto_Integrador.entity.Carrito;
import com.cibertec.Proyecto_Integrador.entity.ItemCarrito;
import com.cibertec.Proyecto_Integrador.entity.Producto;

public interface ItemCarritoRepository extends JpaRepository<ItemCarrito, Long> {

    Optional<ItemCarrito> findByCartAndProduct(Carrito cart, Producto product);

    List<ItemCarrito> findByCart(Carrito cart);

    void deleteByCart(Carrito cart);
}