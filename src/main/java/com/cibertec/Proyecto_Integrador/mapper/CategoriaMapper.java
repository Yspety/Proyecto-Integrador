package com.cibertec.Proyecto_Integrador.mapper;

import org.springframework.stereotype.Component;
import com.cibertec.Proyecto_Integrador.dto.response.CategoriaResponse;
import com.cibertec.Proyecto_Integrador.entity.Categoria;

/** Traduce la entidad Categoria a su DTO de salida. La entidad nunca sale del service. */
@Component
public class CategoriaMapper {

    public CategoriaResponse toResponse(Categoria category) {
        return new CategoriaResponse(
                category.getId(),
                category.getName(),
                category.getDescription());
    }
}
