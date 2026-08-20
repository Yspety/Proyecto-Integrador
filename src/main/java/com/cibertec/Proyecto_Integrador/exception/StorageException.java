package com.cibertec.Proyecto_Integrador.exception;

/** Fallo al guardar, leer o borrar un binario del almacenamiento. Se mapea a 500. */
public class StorageException extends RuntimeException {

    public StorageException(String message) {
        super(message);
    }

    public StorageException(String message, Throwable cause) {
        super(message, cause);
    }
}
