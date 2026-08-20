package com.cibertec.Proyecto_Integrador.dto.response;

import java.util.List;

/**
 * Productos que llegaron a su punto de reposición.
 *
 * <p>{@code total} y {@code sinStock} vienen aparte de la lista para que la UI pueda
 * pintar el contador sin recorrerla, y para poder distinguir la urgencia: un producto
 * bajo mínimo hay que pedirlo; uno en CERO ya te está haciendo perder ventas.
 */
public record AlertaStockResponse(
        int total,
        int sinStock,
        List<ProductoPorReponerRow> productos) {

    /**
     * {@code faltante} es cuánto falta para volver al mínimo ({@code stockMin - stock}).
     * Se calcula acá y no en el front para que el número sea el mismo en la pantalla,
     * en un export y en cualquier cliente futuro.
     */
    public record ProductoPorReponerRow(
            Long productId,
            String sku,
            String name,
            String categoryName,
            int stock,
            int stockMin,
            int faltante) {
    }
}
