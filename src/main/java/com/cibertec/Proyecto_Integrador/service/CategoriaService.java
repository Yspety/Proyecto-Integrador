package com.cibertec.Proyecto_Integrador.service;

import java.util.List;
import com.cibertec.Proyecto_Integrador.dto.request.CategoriaRequest;
import com.cibertec.Proyecto_Integrador.dto.response.CategoriaResponse;

/** Operaciones de catálogo para categorías. La lectura es pública; la escritura, sólo ADMIN. */
public interface CategoriaService {

    /** Listado completo ordenado por nombre. Alimenta el filtro del catálogo. */
    List<CategoriaResponse> listar();

    /** Crea una categoría. Nombre único. */
    CategoriaResponse registrar(CategoriaRequest request);

    /** Actualiza una categoría. Nombre único excluyendo su propio id. */
    CategoriaResponse actualizar(Long id, CategoriaRequest request);

    /**
     * Borra la categoría. A diferencia de Producto, acá es HARD delete: la categoría
     * no tiene flag `active`. Lanza CategoryInUseException (409) si tiene productos.
     */
    void eliminar(Long id);
}
