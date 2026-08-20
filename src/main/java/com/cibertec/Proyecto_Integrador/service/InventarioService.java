package com.cibertec.Proyecto_Integrador.service;

import com.cibertec.Proyecto_Integrador.dto.response.AlertaStockResponse;

/** Gestión de inventario: alertas de reposición sobre el stock que lleva el kardex. */
public interface InventarioService {

    /** Productos activos con {@code stock <= stockMin}, los más críticos primero. */
    AlertaStockResponse porReponer();
}
