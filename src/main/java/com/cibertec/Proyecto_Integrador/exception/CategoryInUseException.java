package com.cibertec.Proyecto_Integrador.exception;

/**
 * Intento de borrar una categoría que todavía tiene productos. Se mapea a 409 Conflict.
 *
 * <p>No se borra en cascada a propósito: los productos usan soft-delete, así que un
 * cascade dejaría filas apuntando a una categoría inexistente.
 */
public class CategoryInUseException extends RuntimeException {
    public CategoryInUseException(String message) {
        super(message);
    }
}
