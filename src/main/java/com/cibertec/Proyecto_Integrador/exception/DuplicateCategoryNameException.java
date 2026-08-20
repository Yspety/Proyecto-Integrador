package com.cibertec.Proyecto_Integrador.exception;

/** Nombre de categoría ya usado por otra fila. Se mapea a 409 Conflict. */
public class DuplicateCategoryNameException extends RuntimeException {
    public DuplicateCategoryNameException(String message) {
        super(message);
    }
}
