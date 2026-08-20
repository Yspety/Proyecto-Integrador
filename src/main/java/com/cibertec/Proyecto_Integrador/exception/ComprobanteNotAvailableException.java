package com.cibertec.Proyecto_Integrador.exception;

/**
 * Se pidió el comprobante de un pedido que no está pagado (PENDIENTE o CANCELADA).
 * Se mapea a 409: el pedido existe, pero su estado actual no admite la operación.
 */
public class ComprobanteNotAvailableException extends RuntimeException {
    public ComprobanteNotAvailableException(String message) {
        super(message);
    }
}
