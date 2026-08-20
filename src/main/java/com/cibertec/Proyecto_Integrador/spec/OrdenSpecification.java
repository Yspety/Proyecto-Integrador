package com.cibertec.Proyecto_Integrador.spec;

import java.time.Instant;
import org.springframework.data.jpa.domain.Specification;
import com.cibertec.Proyecto_Integrador.entity.Orden;
import com.cibertec.Proyecto_Integrador.entity.enums.EstadoOrden;

/**
 * Filtros del listado de pedidos del admin.
 *
 * <p>Mismo contrato que {@link ProductoSpecification}: filtro ausente →
 * {@link Specification#unrestricted()}, NUNCA {@code null}. Spring Data JPA 4 lanza
 * {@code IllegalArgumentException} si se le pasa una spec nula a {@code and()}.
 */
public final class OrdenSpecification {

    private OrdenSpecification() {}

    public static Specification<Orden> hasStatus(EstadoOrden status) {
        if (status == null) {
            return Specification.unrestricted();
        }
        return (root, query, cb) -> cb.equal(root.get("status"), status);
    }

    /**
     * Rango de fecha [from, to): {@code from} inclusivo, {@code to} EXCLUSIVO.
     *
     * <p>El límite superior exclusivo evita el bug clásico de rangos con timestamps:
     * con {@code <=} un pedido de las 14:30 del último día queda afuera, porque la
     * fecha sin hora se interpreta como medianoche.
     */
    public static Specification<Orden> orderDateBetween(Instant from, Instant to) {
        if (from == null && to == null) {
            return Specification.unrestricted();
        }
        return (root, query, cb) -> {
            if (from != null && to != null) {
                return cb.and(
                        cb.greaterThanOrEqualTo(root.get("orderDate"), from),
                        cb.lessThan(root.get("orderDate"), to));
            } else if (from != null) {
                return cb.greaterThanOrEqualTo(root.get("orderDate"), from);
            } else {
                return cb.lessThan(root.get("orderDate"), to);
            }
        };
    }
}
