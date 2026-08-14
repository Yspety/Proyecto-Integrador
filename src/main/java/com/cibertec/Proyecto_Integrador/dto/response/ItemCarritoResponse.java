package com.cibertec.Proyecto_Integrador.dto.response;

import java.math.BigDecimal;

public record ItemCarritoResponse(
        Long itemId,
        Long productId,
        String productName,
        String sku,
        BigDecimal price,
        int quantity,
        BigDecimal subtotal) {
}