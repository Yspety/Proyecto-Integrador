package com.cibertec.Proyecto_Integrador.mapper;

import java.util.List;
import org.springframework.stereotype.Component;
import com.cibertec.Proyecto_Integrador.dto.response.ItemOrdenResponse;
import com.cibertec.Proyecto_Integrador.dto.response.OrdenResponse;
import com.cibertec.Proyecto_Integrador.entity.ItemOrden;
import com.cibertec.Proyecto_Integrador.entity.Orden;

/**
 * Traduce Orden a su DTO. Toca {@code order.getItems()}, que es LAZY: el caller tiene
 * que estar dentro de {@code @Transactional} (mismo patrón que ProductoMapper).
 */
@Component
public class OrdenMapper {

    public OrdenResponse toResponse(Orden order) {
        List<ItemOrdenResponse> items = order.getItems().stream()
                .map(this::toItemResponse)
                .toList();

        return new OrdenResponse(
                order.getId(),
                order.getUser().getId(),
                order.getOrderDate(),
                order.getStatus().name(),
                order.getDocumentType().name(),
                order.getCustomerName(),
                order.getCustomerDoc(),
                order.getSubtotal(),
                order.getDiscount(),
                order.getShippingCost(),
                order.getIgv(),
                order.getTotal(),
                items);
    }

    private ItemOrdenResponse toItemResponse(ItemOrden item) {
        return new ItemOrdenResponse(
                item.getId(),
                item.getProduct().getId(),
                item.getProductName(),   // snapshot, no el nombre actual del producto
                item.getQuantity(),
                item.getUnitPrice(),
                item.getSubtotal());
    }
}
