package com.cibertec.Proyecto_Integrador.exception;

/**
 * Transición de estado que la máquina de estados no permite. Se mapea a 422.
 *
 * <p>422 y no 400: el JSON está bien formado y el estado pedido existe — lo que no es
 * válido es hacer ESE cambio desde el estado actual.
 */
public class OrderStatusTransitionException extends RuntimeException {
    public OrderStatusTransitionException(String message) {
        super(message);
    }
}
