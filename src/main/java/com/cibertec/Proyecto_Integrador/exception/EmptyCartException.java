package com.cibertec.Proyecto_Integrador.exception;

/** Checkout con el carrito vacío. Se mapea a 409. */
public class EmptyCartException extends RuntimeException {
    public EmptyCartException(String message) {
        super(message);
    }
}
