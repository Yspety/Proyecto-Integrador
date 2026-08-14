package com.cibertec.Proyecto_Integrador.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.cibertec.Proyecto_Integrador.entity.Categoria;

public interface CategoriaRepository extends JpaRepository<Categoria, Long> {

    boolean existsByName(String name);

    boolean existsByNameAndIdNot(String name, Long id);
}