package com.cibertec.Proyecto_Integrador.dto.response;

/**
 * Cuerpo único de error de la API. El campo se llama {@code error} porque es
 * exactamente el que lee {@code apiErrorMessage()} en el front (lib/apiError.ts).
 * Cambiar este nombre rompe el mensaje de error de TODAS las pantallas.
 */
public record ApiError(String error) {
}
