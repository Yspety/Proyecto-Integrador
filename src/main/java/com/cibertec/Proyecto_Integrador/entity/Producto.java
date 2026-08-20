package com.cibertec.Proyecto_Integrador.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "products")
@Getter
@Setter
@NoArgsConstructor
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String sku;

    @Column(nullable = false)
    private String name;

    private String description;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @Column(nullable = false)
    private int stock;

    /**
     * Punto de reposición: por debajo o igual a este valor, el producto entra en la
     * alerta de stock bajo.
     *
     * <p>Es POR PRODUCTO y no un umbral global porque no todos se reponen igual: un
     * cable que llega en un día se puede dejar en 2, y una laptop importada que tarda
     * tres semanas necesita avisar mucho antes. Un único número para todo el catálogo
     * alerta tarde para unos y molesta de más para otros — y una alerta que molesta
     * termina ignorada.
     *
     * <p>Default arrancado desde {@code app.inventory.default-stock-min}.
     */
    @Column(name = "stock_min", nullable = false)
    private int stockMin;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(nullable = false)
    private boolean active;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id")
    private Categoria category;

    @OneToMany(mappedBy = "product", fetch = FetchType.LAZY)
    @OrderBy("displayOrder ASC, id ASC")
    private List<ImagenProducto> images = new ArrayList<>();
}