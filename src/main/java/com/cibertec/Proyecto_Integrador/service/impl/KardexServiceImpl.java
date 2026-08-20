package com.cibertec.Proyecto_Integrador.service.impl;

import java.time.Instant;
import org.springframework.stereotype.Service;
import com.cibertec.Proyecto_Integrador.entity.MovimientoStock;
import com.cibertec.Proyecto_Integrador.entity.Producto;
import com.cibertec.Proyecto_Integrador.entity.enums.TipoMovimiento;
import com.cibertec.Proyecto_Integrador.repository.MovimientoStockRepository;
import com.cibertec.Proyecto_Integrador.service.KardexService;

@Service
public class KardexServiceImpl implements KardexService {

    private final MovimientoStockRepository stockMovementRepository;

    public KardexServiceImpl(MovimientoStockRepository stockMovementRepository) {
        this.stockMovementRepository = stockMovementRepository;
    }

    @Override
    public void registrar(Producto product, TipoMovimiento type, int quantity,
                          String reason, String reference) {
        MovimientoStock movement = new MovimientoStock();
        movement.setProduct(product);
        movement.setType(type);
        movement.setQuantity(quantity);
        movement.setReason(reason);
        movement.setReference(reference);
        movement.setCreatedAt(Instant.now());
        stockMovementRepository.save(movement);
    }

    @Override
    public void ajustar(Producto product, int nuevoStock, String reason, String reference) {
        int actual = product.getStock();
        if (nuevoStock == actual) {
            return;   // sin cambio no hay movimiento: el kardex no se ensucia con ruido
        }
        int delta = nuevoStock - actual;
        TipoMovimiento tipo = delta > 0 ? TipoMovimiento.ENTRADA : TipoMovimiento.SALIDA;

        registrar(product, tipo, Math.abs(delta), reason, reference);
        product.setStock(nuevoStock);
    }
}
