package com.cibertec.Proyecto_Integrador.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.cibertec.Proyecto_Integrador.dto.request.CheckoutRequest;
import com.cibertec.Proyecto_Integrador.dto.request.PaymentRequest;
import com.cibertec.Proyecto_Integrador.dto.response.OrdenResponse;
import com.cibertec.Proyecto_Integrador.dto.response.PageResponse;
import com.cibertec.Proyecto_Integrador.entity.ItemCarrito;
import com.cibertec.Proyecto_Integrador.entity.ItemOrden;
import com.cibertec.Proyecto_Integrador.entity.MovimientoStock;
import com.cibertec.Proyecto_Integrador.entity.Orden;
import com.cibertec.Proyecto_Integrador.entity.Producto;
import com.cibertec.Proyecto_Integrador.entity.Usuario;
import com.cibertec.Proyecto_Integrador.entity.enums.EstadoOrden;
import com.cibertec.Proyecto_Integrador.entity.enums.TipoDocumento;
import com.cibertec.Proyecto_Integrador.entity.enums.TipoMovimiento;
import com.cibertec.Proyecto_Integrador.exception.ComprobanteNotAvailableException;
import com.cibertec.Proyecto_Integrador.exception.EmptyCartException;
import com.cibertec.Proyecto_Integrador.exception.InsufficientStockException;
import com.cibertec.Proyecto_Integrador.exception.OrderStatusTransitionException;
import com.cibertec.Proyecto_Integrador.exception.ResourceNotFoundException;
import com.cibertec.Proyecto_Integrador.mapper.OrdenMapper;
import com.cibertec.Proyecto_Integrador.policy.EstadoOrdenPolicy;
import com.cibertec.Proyecto_Integrador.repository.CarritoRepository;
import com.cibertec.Proyecto_Integrador.repository.ItemCarritoRepository;
import com.cibertec.Proyecto_Integrador.repository.OrdenRepository;
import com.cibertec.Proyecto_Integrador.repository.ProductoRepository;
import com.cibertec.Proyecto_Integrador.repository.UsuarioRepository;
import com.cibertec.Proyecto_Integrador.service.ComprobanteGenerator;
import com.cibertec.Proyecto_Integrador.service.KardexService;
import com.cibertec.Proyecto_Integrador.service.OrdenService;
import com.cibertec.Proyecto_Integrador.spec.OrdenSpecification;

@Service
public class OrdenServiceImpl implements OrdenService {

    private final OrdenRepository orderRepository;
    private final CarritoRepository cartRepository;
    private final ItemCarritoRepository cartItemRepository;
    private final ProductoRepository productRepository;
    private final UsuarioRepository userRepository;
    private final KardexService kardexService;
    private final OrdenMapper orderMapper;
    private final ComprobanteGenerator comprobanteGenerator;

    private final BigDecimal igvRate;
    private final BigDecimal freeShippingFrom;
    private final BigDecimal shippingCost;

    public OrdenServiceImpl(OrdenRepository orderRepository,
                            CarritoRepository cartRepository,
                            ItemCarritoRepository cartItemRepository,
                            ProductoRepository productRepository,
                            UsuarioRepository userRepository,
                            KardexService kardexService,
                            OrdenMapper orderMapper,
                            ComprobanteGenerator comprobanteGenerator,
                            @Value("${app.orders.igv-rate:0.18}") BigDecimal igvRate,
                            @Value("${app.orders.free-shipping-from:300}") BigDecimal freeShippingFrom,
                            @Value("${app.orders.shipping-cost:20}") BigDecimal shippingCost) {
        this.orderRepository = orderRepository;
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
        this.kardexService = kardexService;
        this.orderMapper = orderMapper;
        this.comprobanteGenerator = comprobanteGenerator;
        this.igvRate = igvRate;
        this.freeShippingFrom = freeShippingFrom;
        this.shippingCost = shippingCost;
    }

    // ─── checkout ───────────────────────────────────────────────────────────────

    /**
     * Carrito → pedido, en UNA transacción: descuenta stock, registra el kardex y vacía
     * el carrito. Si algo falla (stock insuficiente en la última línea, por ejemplo) se
     * revierte todo: no queda ni el pedido a medias ni stock descontado de más.
     */
    @Override
    @Transactional
    public OrdenResponse confirmarCompra(String email, CheckoutRequest request) {
        Usuario user = resolveUser(email);
        validarComprobante(request);

        List<ItemCarrito> cartItems = cartRepository.findByUser(user)
                .map(cartItemRepository::findByCart)
                .orElseGet(List::of);

        if (cartItems.isEmpty()) {
            throw new EmptyCartException("El carrito está vacío");
        }

        Orden order = new Orden();
        order.setUser(user);
        order.setOrderDate(Instant.now());
        order.setStatus(EstadoOrden.PENDIENTE);
        order.setDocumentType(request.documentType());
        order.setCustomerName(request.customerName());
        order.setCustomerDoc(request.customerDoc());

        BigDecimal subtotal = BigDecimal.ZERO;

        // Se bloquean los productos SIEMPRE en el mismo orden (por id). Dos checkouts
        // simultáneos que compartan productos toman los locks en la misma secuencia y
        // por lo tanto no pueden quedar cada uno esperando al lock del otro (deadlock).
        List<ItemCarrito> ordenados = cartItems.stream()
                .sorted(Comparator.comparing(item -> item.getProduct().getId()))
                .toList();

        for (ItemCarrito cartItem : ordenados) {
            // findByIdWithLock (PESSIMISTIC_WRITE): serializa el chequeo-y-descuento de
            // stock. Sin el lock, dos compras del último artículo leen "queda 1" y las
            // dos descuentan: stock negativo y una venta que no se puede cumplir.
            Producto product = productRepository.findByIdWithLock(cartItem.getProduct().getId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Producto no encontrado: " + cartItem.getProduct().getId()));

            if (!product.isActive()) {
                throw new ResourceNotFoundException("Producto no disponible: " + product.getId());
            }
            int qty = cartItem.getQuantity();
            if (qty > product.getStock()) {
                throw new InsufficientStockException(
                        "Stock insuficiente para el producto " + product.getId()
                        + ": solicitado=" + qty + ", disponible=" + product.getStock());
            }

            product.setStock(product.getStock() - qty);
            productRepository.save(product);

            BigDecimal unitPrice = product.getPrice();
            BigDecimal lineTotal = unitPrice.multiply(BigDecimal.valueOf(qty)).setScale(2, RoundingMode.HALF_UP);

            ItemOrden orderItem = new ItemOrden();
            orderItem.setProduct(product);
            orderItem.setProductName(product.getName());   // snapshot
            orderItem.setQuantity(qty);
            orderItem.setUnitPrice(unitPrice);
            orderItem.setSubtotal(lineTotal);
            order.addItem(orderItem);

            subtotal = subtotal.add(lineTotal);
        }

        aplicarImportes(order, subtotal);
        Orden saved = orderRepository.save(order);

        // El kardex se escribe DESPUÉS del save para poder referenciar el id del pedido.
        for (ItemOrden item : saved.getItems()) {
            kardexService.registrar(item.getProduct(), TipoMovimiento.SALIDA, item.getQuantity(),
                    "Checkout", "ORDEN-" + saved.getId());
        }

        cartRepository.findByUser(user).ifPresent(cartItemRepository::deleteByCart);

        return orderMapper.toResponse(saved);
    }

    // ─── consultas del cliente ──────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public List<OrdenResponse> misOrdenes(String email) {
        Usuario user = resolveUser(email);
        return orderRepository.findByUserOrderByOrderDateDesc(user).stream()
                .map(orderMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public OrdenResponse miOrden(String email, Long orderId) {
        return orderMapper.toResponse(findOwnedOrThrow(email, orderId));
    }

    // ─── pago ───────────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public OrdenResponse pagar(String email, Long orderId, PaymentRequest request) {
        Orden order = findOwnedOrThrow(email, orderId);

        if (!EstadoOrdenPolicy.puedeTransicionar(order.getStatus(), EstadoOrden.CONFIRMADA)) {
            throw new OrderStatusTransitionException(
                    "No se puede pagar un pedido en estado " + order.getStatus());
        }

        order.setStatus(EstadoOrden.CONFIRMADA);
        order.setPaymentMethod(request.method());
        order.setPaidAt(Instant.now());
        return orderMapper.toResponse(orderRepository.save(order));
    }

    // ─── admin ──────────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public PageResponse<OrdenResponse> listarOrdenes(EstadoOrden status, Instant from, Instant to,
                                                     Pageable pageable) {
        Specification<Orden> spec = Specification
                .where(OrdenSpecification.hasStatus(status))
                .and(OrdenSpecification.orderDateBetween(from, to));

        Page<OrdenResponse> page = orderRepository.findAll(spec, pageable)
                .map(orderMapper::toResponse);

        return PageResponse.of(page);
    }

    @Override
    @Transactional(readOnly = true)
    public OrdenResponse obtenerOrden(Long orderId) {
        return orderMapper.toResponse(findOrThrow(orderId));
    }

    /**
     * Cambia el estado respetando {@link EstadoOrdenPolicy}. Cancelar repone el stock
     * y deja el rastro en el kardex — nunca se toca {@code Producto.stock} sin registrar
     * el movimiento que lo explica.
     */
    @Override
    @Transactional
    public OrdenResponse actualizarEstado(Long orderId, EstadoOrden newStatus) {
        Orden order = findOrThrow(orderId);
        EstadoOrden current = order.getStatus();

        if (!EstadoOrdenPolicy.puedeTransicionar(current, newStatus)) {
            throw new OrderStatusTransitionException(
                    "Transición no permitida: " + current + " → " + newStatus
                    + ". Estados posibles: " + EstadoOrdenPolicy.siguientes(current));
        }

        if (EstadoOrdenPolicy.reponeStock(current, newStatus)) {
            for (ItemOrden item : order.getItems()) {
                Producto product = productRepository.findByIdWithLock(item.getProduct().getId())
                        .orElseThrow(() -> new ResourceNotFoundException(
                                "Producto no encontrado: " + item.getProduct().getId()));
                product.setStock(product.getStock() + item.getQuantity());
                productRepository.save(product);

                kardexService.registrar(product, TipoMovimiento.ENTRADA, item.getQuantity(),
                        "Cancelación de pedido", "ORDEN-" + order.getId());
            }
        }

        order.setStatus(newStatus);
        return orderMapper.toResponse(orderRepository.save(order));
    }

    // ─── comprobante ────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public byte[] miComprobantePdf(String email, Long orderId) {
        return comprobanteDe(findOwnedOrThrow(email, orderId));
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] comprobantePdf(Long orderId) {
        return comprobanteDe(findOrThrow(orderId));
    }

    // ─── helpers ────────────────────────────────────────────────────────────────

    private byte[] comprobanteDe(Orden order) {
        if (!EstadoOrdenPolicy.estaPagado(order.getStatus())) {
            throw new ComprobanteNotAvailableException(
                    "El pedido no tiene comprobante disponible en estado " + order.getStatus());
        }
        return comprobanteGenerator.generar(order);
    }

    /**
     * Calcula envío, total e IGV.
     *
     * <p>El precio del catálogo YA incluye IGV, así que el impuesto se desglosa hacia
     * ADENTRO: {@code base = total / (1 + tasa)} e {@code igv = total - base}. Restar en
     * vez de multiplicar la base por la tasa garantiza que {@code base + igv} dé
     * exactamente el total, sin descuadres de un centavo por redondeo.
     */
    private void aplicarImportes(Orden order, BigDecimal subtotal) {
        subtotal = subtotal.setScale(2, RoundingMode.HALF_UP);
        BigDecimal discount = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);

        BigDecimal envio = subtotal.compareTo(freeShippingFrom) >= 0
                ? BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP)
                : shippingCost.setScale(2, RoundingMode.HALF_UP);

        BigDecimal total = subtotal.subtract(discount).add(envio).setScale(2, RoundingMode.HALF_UP);
        BigDecimal base = total.divide(BigDecimal.ONE.add(igvRate), 2, RoundingMode.HALF_UP);
        BigDecimal igv = total.subtract(base);

        order.setSubtotal(subtotal);
        order.setDiscount(discount);
        order.setShippingCost(envio);
        order.setTotal(total);
        order.setIgv(igv);
    }

    /** BOLETA exige DNI (8 dígitos); FACTURA exige RUC (11). El formato genérico ya lo validó el DTO. */
    private void validarComprobante(CheckoutRequest request) {
        int len = request.customerDoc().length();
        if (request.documentType() == TipoDocumento.BOLETA && len != 8) {
            throw new IllegalArgumentException("La boleta requiere un DNI de 8 dígitos");
        }
        if (request.documentType() == TipoDocumento.FACTURA && len != 11) {
            throw new IllegalArgumentException("La factura requiere un RUC de 11 dígitos");
        }
    }

    private Usuario resolveUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + email));
    }

    /** 404 y no 403 cuando el pedido es de otro: no revelar qué ids existen. */
    private Orden findOwnedOrThrow(String email, Long orderId) {
        Usuario user = resolveUser(email);
        return orderRepository.findByIdAndUser(orderId, user)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido no encontrado: " + orderId));
    }

    private Orden findOrThrow(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido no encontrado: " + orderId));
    }
}
