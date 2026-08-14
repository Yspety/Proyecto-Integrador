package com.cibertec.Proyecto_Integrador.mapper;

import java.math.BigDecimal;
import java.util.List;
import org.springframework.stereotype.Component;
import com.cibertec.Proyecto_Integrador.dto.response.ItemCarritoResponse;
import com.cibertec.Proyecto_Integrador.dto.response.CarritoResponse;
import com.cibertec.Proyecto_Integrador.entity.Carrito;
import com.cibertec.Proyecto_Integrador.entity.ItemCarrito;
import com.cibertec.Proyecto_Integrador.entity.Producto;

/** Manual mapper — no MapStruct, no @OneToMany on Carrito. Items are passed in explicitly. */
@Component
public class CarritoMapper {

    public ItemCarritoResponse toItemResponse(ItemCarrito item) {
        Producto product = item.getProduct();
        BigDecimal subtotal = product.getPrice()
                .multiply(BigDecimal.valueOf(item.getQuantity()));
        return new ItemCarritoResponse(
                item.getId(),
                product.getId(),
                product.getName(),
                product.getSku(),
                product.getPrice(),
                item.getQuantity(),
                subtotal);
    }

    public CarritoResponse toResponse(Carrito cart, List<ItemCarrito> items) {
        List<ItemCarritoResponse> itemResponses = items.stream()
                .map(this::toItemResponse)
                .toList();
        BigDecimal total = itemResponses.stream()
                .map(ItemCarritoResponse::subtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return new CarritoResponse(
                cart.getId(),
                itemResponses,
                total,
                cart.getUpdatedAt());
    }

    public CarritoResponse emptyCart() {
        return new CarritoResponse(null, List.of(), BigDecimal.ZERO, null);
    }
}