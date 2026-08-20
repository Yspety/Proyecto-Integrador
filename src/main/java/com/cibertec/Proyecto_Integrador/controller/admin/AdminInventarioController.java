package com.cibertec.Proyecto_Integrador.controller.admin;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.cibertec.Proyecto_Integrador.dto.response.AlertaStockResponse;
import com.cibertec.Proyecto_Integrador.service.InventarioService;

/**
 * Gestión de inventario — sólo ADMIN.
 *
 * <p>Namespace propio (y no bajo /reports) porque esto no es un informe histórico: es
 * una vista operativa del estado de HOY, sobre la que se actúa comprando.
 */
@RestController
@RequestMapping("/api/admin/inventory")
public class AdminInventarioController {

    private final InventarioService inventoryService;

    public AdminInventarioController(InventarioService inventoryService) {
        this.inventoryService = inventoryService;
    }

    /** GET /api/admin/inventory/low-stock → productos activos en su punto de reposición o por debajo. */
    @GetMapping("/low-stock")
    public AlertaStockResponse porReponer() {
        return inventoryService.porReponer();
    }
}
