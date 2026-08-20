package com.cibertec.Proyecto_Integrador.service.impl;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.cibertec.Proyecto_Integrador.dto.response.AlertaStockResponse;
import com.cibertec.Proyecto_Integrador.dto.response.AlertaStockResponse.ProductoPorReponerRow;
import com.cibertec.Proyecto_Integrador.entity.Producto;
import com.cibertec.Proyecto_Integrador.repository.ProductoRepository;
import com.cibertec.Proyecto_Integrador.service.InventarioService;

@Service
public class InventarioServiceImpl implements InventarioService {

    private final ProductoRepository productRepository;

    public InventarioServiceImpl(ProductoRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public AlertaStockResponse porReponer() {
        List<Producto> criticos = productRepository.findPorReponer();

        // toResponse() del mapper traería la categoría igual, pero acá se arma una fila
        // propia: la alerta necesita `faltante`, que no es un dato del producto sino de
        // la comparación contra su mínimo.
        List<ProductoPorReponerRow> filas = criticos.stream()
                .map(p -> new ProductoPorReponerRow(
                        p.getId(),
                        p.getSku(),
                        p.getName(),
                        p.getCategory().getName(),   // LAZY: por eso el @Transactional
                        p.getStock(),
                        p.getStockMin(),
                        Math.max(0, p.getStockMin() - p.getStock())))
                .toList();

        int sinStock = (int) criticos.stream().filter(p -> p.getStock() == 0).count();

        return new AlertaStockResponse(filas.size(), sinStock, filas);
    }
}
