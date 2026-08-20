package com.cibertec.Proyecto_Integrador.spec;

import java.math.BigDecimal;
import org.springframework.data.jpa.domain.Specification;
import com.cibertec.Proyecto_Integrador.entity.Producto;

/**
 * Fábricas de predicados JPA para búsqueda de productos.
 *
 * <p>Contrato: cuando el filtro está ausente se devuelve {@link Specification#unrestricted()}
 * (una spec que no restringe nada), NO {@code null}.
 *
 * <p>Históricamente estos métodos devolvían {@code null} y Spring Data lo ignoraba al
 * componer. Desde Spring Data JPA 4 eso dejó de ser válido: {@code and(null)} lanza
 * {@code IllegalArgumentException: Other specification must not be null}. Devolver una
 * spec neutra compone igual de limpio y no depende de que el framework tolere nulls.
 */
public final class ProductoSpecification {

    private ProductoSpecification() {}

    /** Búsqueda por nombre (LIKE case-insensitive). Neutra cuando {@code name} es null o blank. */
    public static Specification<Producto> nameLike(String name) {
        if (name == null || name.isBlank()) {
            return Specification.unrestricted();
        }
        return (root, query, cb) ->
                cb.like(cb.lower(root.get("name")), "%" + name.toLowerCase() + "%");
    }

    /** Filtro por categoría. Neutra cuando {@code categoryId} es null. */
    public static Specification<Producto> hasCategory(Long categoryId) {
        if (categoryId == null) {
            return Specification.unrestricted();
        }
        return (root, query, cb) ->
                cb.equal(root.get("category").get("id"), categoryId);
    }

    /**
     * Filtro por rango de precio.
     * Neutra cuando ambos límites son null.
     * Si sólo uno está presente se aplica ge o le según corresponda.
     */
    public static Specification<Producto> priceBetween(BigDecimal min, BigDecimal max) {
        if (min == null && max == null) {
            return Specification.unrestricted();
        }
        return (root, query, cb) -> {
            if (min != null && max != null) {
                return cb.between(root.get("price"), min, max);
            } else if (min != null) {
                return cb.greaterThanOrEqualTo(root.get("price"), min);
            } else {
                return cb.lessThanOrEqualTo(root.get("price"), max);
            }
        };
    }

    /** Filtro por estado activo. Neutra cuando {@code active} es null. */
    public static Specification<Producto> isActive(Boolean active) {
        if (active == null) {
            return Specification.unrestricted();
        }
        return (root, query, cb) ->
                cb.equal(root.get("active"), active);
    }
}
