package com.cibertec.Proyecto_Integrador.service.impl;

import java.math.BigDecimal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.cibertec.Proyecto_Integrador.dto.request.ProductoRequest;
import com.cibertec.Proyecto_Integrador.dto.response.PageResponse;
import com.cibertec.Proyecto_Integrador.dto.response.ProductoResponse;
import com.cibertec.Proyecto_Integrador.exception.DuplicateSkuException;
import com.cibertec.Proyecto_Integrador.exception.ResourceNotFoundException;
import com.cibertec.Proyecto_Integrador.mapper.ProductoMapper;
import com.cibertec.Proyecto_Integrador.entity.Categoria;
import com.cibertec.Proyecto_Integrador.entity.Producto;
import com.cibertec.Proyecto_Integrador.repository.CategoriaRepository;
import com.cibertec.Proyecto_Integrador.repository.ProductoRepository;
import com.cibertec.Proyecto_Integrador.service.KardexService;
import com.cibertec.Proyecto_Integrador.service.ProductoService;
import com.cibertec.Proyecto_Integrador.spec.ProductoSpecification;

@Service
public class ProductoServiceImpl extends ICRUDImpl<Producto, Long> implements ProductoService {

    private final ProductoRepository productRepository;
    private final CategoriaRepository categoryRepository;
    private final ProductoMapper productMapper;
    private final KardexService kardexService;
    private final int defaultStockMin;

    public ProductoServiceImpl(ProductoRepository productRepository,
                               CategoriaRepository categoryRepository,
                               ProductoMapper productMapper,
                               KardexService kardexService,
                               @Value("${app.inventory.default-stock-min:5}") int defaultStockMin) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.productMapper = productMapper;
        this.kardexService = kardexService;
        this.defaultStockMin = defaultStockMin;
    }

    /** Repository que usa el CRUD genérico heredado (guardar/listarTodos/...). */
    @Override
    protected JpaRepository<Producto, Long> repo() {
        return productRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ProductoResponse> buscar(String name, Long categoryId,
                                                 BigDecimal priceMin, BigDecimal priceMax,
                                                 Pageable pageable) {
        Specification<Producto> spec = Specification
                .where(ProductoSpecification.isActive(true))
                .and(ProductoSpecification.nameLike(name))
                .and(ProductoSpecification.hasCategory(categoryId))
                .and(ProductoSpecification.priceBetween(priceMin, priceMax));

        Page<ProductoResponse> page = productRepository
                .findAll(spec, pageable)
                .map(productMapper::toResponse);

        return PageResponse.of(page);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductoResponse buscarPorId(Long id) {
        Producto product = findOrThrow(id);
        if (!product.isActive()) {
            throw new ResourceNotFoundException("Producto no encontrado: " + id);
        }
        // toResponseWithImages() accesses the LAZY images collection — must remain inside @Transactional
        return productMapper.toResponseWithImages(product);
    }

    @Override
    @Transactional
    public ProductoResponse registrar(ProductoRequest request) {
        if (productRepository.existsBySku(request.sku())) {
            throw new DuplicateSkuException("El SKU ya está registrado: " + request.sku());
        }
        Categoria category = findCategoryOrThrow(request.categoryId());

        Producto product = new Producto();
        product.setSku(request.sku());
        product.setName(request.name());
        product.setDescription(request.description());
        product.setPrice(request.price());
        product.setStock(request.stock() != null ? request.stock() : 0);
        product.setStockMin(request.stockMin() != null ? request.stockMin() : defaultStockMin);
        product.setImageUrl(request.imageUrl());
        product.setActive(true);
        product.setCategory(category);

        return productMapper.toResponse(guardar(product));   // ← heredado de ICRUDImpl
    }

    /**
     * Edita el producto. Un cambio de {@code stock} se aplica como AJUSTE DE INVENTARIO:
     * queda registrado en el kardex como ENTRADA o SALIDA según el sentido.
     *
     * <p>Antes el stock del request se descartaba en silencio para proteger la invariante
     * "el stock no cambia sin un movimiento que lo explique". La invariante era correcta;
     * descartar el dato sin avisar, no: el formulario decía "guardado" y no pasaba nada.
     * Ahora se respeta la invariante Y se honra la edición.
     *
     * <p>Se lee con lock pesimista porque el ajuste es un read-modify-write sobre el mismo
     * stock que puede estar descontando un checkout en paralelo.
     */
    @Override
    @Transactional
    public ProductoResponse actualizar(Long id, ProductoRequest request) {
        Producto product = productRepository.findByIdWithLock(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado: " + id));

        if (productRepository.existsBySkuAndIdNot(request.sku(), id)) {
            throw new DuplicateSkuException("El SKU ya está registrado en otro producto: " + request.sku());
        }
        Categoria category = findCategoryOrThrow(request.categoryId());

        product.setSku(request.sku());
        product.setName(request.name());
        product.setDescription(request.description());
        product.setPrice(request.price());
        product.setImageUrl(request.imageUrl());
        product.setCategory(category);

        // null = "no lo estoy tocando", no "poneme cero": conserva el valor actual.
        if (request.stockMin() != null) {
            product.setStockMin(request.stockMin());
        }

        if (request.stock() != null) {
            kardexService.ajustar(product, request.stock(), "Ajuste de inventario", "AJUSTE-PROD-" + id);
        }

        return productMapper.toResponse(guardar(product));   // ← heredado de ICRUDImpl
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        Producto product = findOrThrow(id);
        // SOFT delete: marca como inactivo, no elimina la fila.
        // No usamos el borrar() genérico (haría hard delete por id): la lógica es propia.
        product.setActive(false);
        productRepository.save(product);
    }

    // ─── private helpers ────────────────────────────────────────────────────────

    private Producto findOrThrow(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado: " + id));
    }

    private Categoria findCategoryOrThrow(Long categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada: " + categoryId));
    }
}