package com.cibertec.Proyecto_Integrador.entity;

import jakarta.persistence.*;
import java.time.Instant;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "product_image")
@Getter
@Setter
@NoArgsConstructor
public class ImagenProducto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Producto product;

    @Column(nullable = false, length = 500)
    private String path;

    @Column(name = "display_order", nullable = false)
    private short displayOrder;

    @Column(name = "is_cover", nullable = false)
    private boolean isCover;

    /**
     * Lo setea el service, igual que Usuario.createdAt y Carrito.createdAt.
     *
     * <p>Antes estaba como {@code insertable = false}, que le delega el valor a un DEFAULT
     * de la base. Pero ddl-auto genera la columna NOT NULL SIN default, así que el INSERT
     * omitía el campo y MySQL lo rechazaba con "Field 'created_at' doesn't have a default
     * value". Manejarlo desde la aplicación además evita atarse a la sintaxis de MySQL.
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}