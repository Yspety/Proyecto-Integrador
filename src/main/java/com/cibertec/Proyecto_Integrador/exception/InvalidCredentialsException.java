package com.cibertec.Proyecto_Integrador.exception;

/**
 * Credenciales inválidas (email inexistente, password incorrecto o usuario
 * inactivo — el mismo error para no filtrar qué emails existen). Se mapea a 401.
 */
public class InvalidCredentialsException extends RuntimeException {
    public InvalidCredentialsException(String message) {
        super(message);
    }
}