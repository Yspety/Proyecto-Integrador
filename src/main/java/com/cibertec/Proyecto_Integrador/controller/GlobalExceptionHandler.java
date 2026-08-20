package com.cibertec.Proyecto_Integrador.controller;

import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import com.cibertec.Proyecto_Integrador.dto.response.ApiError;
import com.cibertec.Proyecto_Integrador.exception.CategoryInUseException;
import com.cibertec.Proyecto_Integrador.exception.ComprobanteNotAvailableException;
import com.cibertec.Proyecto_Integrador.exception.DuplicateCategoryNameException;
import com.cibertec.Proyecto_Integrador.exception.DuplicateEmailException;
import com.cibertec.Proyecto_Integrador.exception.EmptyCartException;
import com.cibertec.Proyecto_Integrador.exception.OrderStatusTransitionException;
import com.cibertec.Proyecto_Integrador.exception.DuplicateSkuException;
import com.cibertec.Proyecto_Integrador.exception.InsufficientStockException;
import com.cibertec.Proyecto_Integrador.exception.InvalidCredentialsException;
import com.cibertec.Proyecto_Integrador.exception.LastAdminException;
import com.cibertec.Proyecto_Integrador.exception.ResourceNotFoundException;
import com.cibertec.Proyecto_Integrador.exception.StorageException;

/**
 * Traduce las excepciones de dominio al status HTTP correcto y al cuerpo {@link ApiError}.
 *
 * <p>Sin esto, cada excepción de service sale como 500 y el front sólo puede mostrar
 * su mensaje de fallback.
 *
 * <p>A propósito NO hay un handler de {@code Exception.class}: se comería las
 * excepciones propias de Spring (ruta inexistente, método no soportado) y las
 * convertiría en 500, escondiendo 404 y 405 legítimos.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /** Recurso inexistente — también el caso IDOR, que responde 404 y no 403 para no filtrar existencia. */
    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError handleNotFound(ResourceNotFoundException ex) {
        return new ApiError(ex.getMessage());
    }

    /** Credenciales inválidas o usuario inactivo. El mensaje es siempre el mismo: no filtrar qué emails existen. */
    @ExceptionHandler(InvalidCredentialsException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ApiError handleInvalidCredentials(InvalidCredentialsException ex) {
        return new ApiError(ex.getMessage());
    }

    /** Choques con el estado actual: duplicados, stock insuficiente, último admin, categoría en uso. */
    @ExceptionHandler({
            DuplicateEmailException.class,
            DuplicateSkuException.class,
            DuplicateCategoryNameException.class,
            InsufficientStockException.class,
            LastAdminException.class,
            CategoryInUseException.class,
            EmptyCartException.class,
            ComprobanteNotAvailableException.class})
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleConflict(RuntimeException ex) {
        return new ApiError(ex.getMessage());
    }

    /**
     * Transición de estado ilegal (PENDIENTE → ENVIADO, cancelar algo ya entregado…).
     * 422 y no 409: el request está bien formado y el estado destino existe; lo
     * inválido es hacer ese salto desde el estado actual.
     */
    @ExceptionHandler(OrderStatusTransitionException.class)
    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    public ApiError handleStatusTransition(OrderStatusTransitionException ex) {
        return new ApiError(ex.getMessage());
    }

    /**
     * Body que no pasa Jakarta Validation (@NotBlank, @Email, …).
     * Junta los errores de campo en un solo mensaje legible: "email: no debe estar vacío".
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleValidation(MethodArgumentNotValidException ex) {
        String detail = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return new ApiError(detail.isBlank() ? "Datos inválidos" : detail);
    }

    /** Argumentos rechazados por reglas de negocio (tipo de archivo, tamaño, IDs de reordenamiento). */
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleIllegalArgument(IllegalArgumentException ex) {
        return new ApiError(ex.getMessage());
    }

    /** El archivo excede spring.servlet.multipart.max-file-size. */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    @ResponseStatus(HttpStatus.PAYLOAD_TOO_LARGE)
    public ApiError handleMaxUploadSize(MaxUploadSizeExceededException ex) {
        return new ApiError("El archivo supera el tamaño máximo permitido (5 MB).");
    }

    /**
     * Violación de constraint que no atrapó ninguna regla de negocio (unique, FK, NOT NULL).
     * Se loguea COMPLETA: casi siempre delata un bug de mapeo, no un conflicto legítimo
     * del usuario. Sin este handler sale como 500 con el cuerpo por defecto de Spring,
     * que no tiene el campo `error` que el front sabe leer.
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleDataIntegrity(DataIntegrityViolationException ex) {
        log.error("Violación de integridad de datos", ex);
        return new ApiError("La operación viola una restricción de datos.");
    }

    /** Fallo de disco: es del servidor, así que se loguea completo y al cliente sólo le llega un mensaje genérico. */
    @ExceptionHandler(StorageException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiError handleStorage(StorageException ex) {
        log.error("Fallo de almacenamiento", ex);
        return new ApiError("No se pudo procesar el archivo.");
    }
}
