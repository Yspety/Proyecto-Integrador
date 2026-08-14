package com.cibertec.Proyecto_Integrador.service;

import com.cibertec.Proyecto_Integrador.dto.request.ItemCarritoRequest;
import com.cibertec.Proyecto_Integrador.dto.request.UpdateQuantityRequest;
import com.cibertec.Proyecto_Integrador.dto.response.CarritoResponse;

public interface CarritoService {

    CarritoResponse obtenerCarrito(String email);

    CarritoResponse agregarItem(String email, ItemCarritoRequest request);

    /** Public so Spring proxy can intercept. Tx1: insert path. */
    CarritoResponse intentarAgregarItem(String email, ItemCarritoRequest request);

    /** Public so Spring proxy can intercept. Tx2: merge path after constraint violation. */
    CarritoResponse fusionarEnConflicto(String email, ItemCarritoRequest request);

    CarritoResponse actualizarItem(String email, Long itemId, UpdateQuantityRequest request);

    void quitarItem(String email, Long itemId);

    void vaciarCarrito(String email);
}