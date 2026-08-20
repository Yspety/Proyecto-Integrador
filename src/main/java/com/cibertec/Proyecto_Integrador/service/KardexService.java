package com.cibertec.Proyecto_Integrador.service;

import com.cibertec.Proyecto_Integrador.entity.Producto;
import com.cibertec.Proyecto_Integrador.entity.enums.TipoMovimiento;

/**
 * Único punto por el que se escribe el kardex.
 *
 * <p>Existe para sostener la invariante del sistema: {@code Producto.stock} NUNCA cambia
 * sin una fila de MovimientoStock que lo explique. Mientras todos los que tocan stock
 * pasen por acá, el saldo y el historial no se pueden desincronizar.
 */
public interface KardexService {

    /** Registra un movimiento. {@code quantity} siempre positiva: el sentido lo da {@code type}. */
    void registrar(Producto product, TipoMovimiento type, int quantity, String reason, String reference);

    /**
     * Lleva el stock del producto a {@code nuevoStock} dejando el movimiento de ajuste
     * correspondiente (ENTRADA si sube, SALIDA si baja). No-op si no hay diferencia.
     *
     * <p>MUTA la entidad pero NO la persiste: el caller ya está dentro de su transacción
     * y hace el save.
     */
    void ajustar(Producto product, int nuevoStock, String reason, String reference);
}
