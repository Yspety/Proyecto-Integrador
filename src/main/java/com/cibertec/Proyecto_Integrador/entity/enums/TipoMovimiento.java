package com.cibertec.Proyecto_Integrador.entity.enums;

/**
 * Sentido de un movimiento de kardex.
 *
 * <p>SALIDA descuenta stock (checkout), ENTRADA lo repone (cancelación de pedido).
 * El kardex es append-only: nunca se edita ni se borra un movimiento — un error se
 * corrige con el movimiento inverso, igual que en contabilidad.
 */
public enum TipoMovimiento {
    ENTRADA,
    SALIDA
}
