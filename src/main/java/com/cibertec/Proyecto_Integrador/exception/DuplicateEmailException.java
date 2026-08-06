package com.cibertec.Proyecto_Integrador.exception;

/** El email ya está registrado. Se mapea a 409 Conflict. */
public class DuplicateEmailException extends RuntimeException {
    public DuplicateEmailException(String message) {
        super(message);
    }
}