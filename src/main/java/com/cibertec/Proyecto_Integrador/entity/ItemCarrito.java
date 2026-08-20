package com.cibertec.Proyecto_Integrador.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Línea del carrito. UN producto aparece como máximo UNA vez por carrito — de ahí el
 * UNIQUE(cart_id, product_id).
 *
 * <p>Esa restricción NO es decorativa: {@code CarritoServiceImpl.agregarItem()} está
 * construido encima de ella. Inserta con {@code saveAndFlush} para que una colisión
 * estalle como DataIntegrityViolationException y poder reintentar fusionando en una
 * segunda transacción. Sin el UNIQUE, dos requests concurrentes del mismo producto
 * crean DOS filas y el producto aparece duplicado en el carrito — y todo ese mecanismo
 * de reintento queda como código muerto que nunca se ejecuta.
 */
@Entity
@Table(name = "cart_item", uniqueConstraints = @UniqueConstraint(
        name = "uk_cart_item_cart_product", columnNames = {"cart_id", "product_id"}))
@Getter
@Setter
@NoArgsConstructor
public class ItemCarrito {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cart_id")
    private Carrito cart;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id")
    private Producto product;

    @Column(nullable = false)
    private int quantity;
}