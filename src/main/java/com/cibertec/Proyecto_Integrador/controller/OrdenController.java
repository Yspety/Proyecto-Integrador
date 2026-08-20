package com.cibertec.Proyecto_Integrador.controller;

import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import com.cibertec.Proyecto_Integrador.dto.request.CheckoutRequest;
import com.cibertec.Proyecto_Integrador.dto.request.PaymentRequest;
import com.cibertec.Proyecto_Integrador.dto.response.OrdenResponse;
import com.cibertec.Proyecto_Integrador.service.OrdenService;

/**
 * Pedidos del cliente autenticado.
 *
 * <p>Igual que el carrito: el dueño sale del JWT, nunca de la URL. Un pedido ajeno
 * responde 404 (no 403) para no confirmar qué ids existen.
 */
@RestController
@RequestMapping("/api/orders")
public class OrdenController {

    private final OrdenService orderService;

    public OrdenController(OrdenService orderService) {
        this.orderService = orderService;
    }

    /**
     * POST /api/orders/checkout → 201 OrdenResponse en estado PENDIENTE.
     * Descuenta stock, escribe el kardex y vacía el carrito, todo en una transacción.
     * 409 si el carrito está vacío o falta stock; 400 si el documento no corresponde
     * al tipo de comprobante (BOLETA→DNI, FACTURA→RUC).
     */
    @PostMapping("/checkout")
    @ResponseStatus(HttpStatus.CREATED)
    public OrdenResponse confirmarCompra(@AuthenticationPrincipal String email,
                                         @Valid @RequestBody CheckoutRequest request) {
        return orderService.confirmarCompra(email, request);
    }

    /** GET /api/orders → 200, pedidos propios más nuevos primero. */
    @GetMapping
    public List<OrdenResponse> misOrdenes(@AuthenticationPrincipal String email) {
        return orderService.misOrdenes(email);
    }

    /** GET /api/orders/{id} → 200 OrdenResponse. 404 si no es propio. */
    @GetMapping("/{id}")
    public OrdenResponse miOrden(@AuthenticationPrincipal String email, @PathVariable Long id) {
        return orderService.miOrden(email, id);
    }

    /**
     * POST /api/orders/{id}/pay → 200. Pago SIMULADO: PENDIENTE → CONFIRMADA.
     * No hay pasarela real; sólo se registra el método y la fecha.
     * 422 si el pedido no está PENDIENTE.
     */
    @PostMapping("/{id}/pay")
    public OrdenResponse pagar(@AuthenticationPrincipal String email,
                               @PathVariable Long id,
                               @Valid @RequestBody PaymentRequest request) {
        return orderService.pagar(email, id, request);
    }

    /** GET /api/orders/{id}/comprobante → 200 application/pdf. 409 si el pedido no está pagado. */
    @GetMapping(value = "/{id}/comprobante", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> comprobante(@AuthenticationPrincipal String email,
                                              @PathVariable Long id) {
        byte[] pdf = orderService.miComprobantePdf(email, id);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(
                ContentDisposition.attachment().filename("comprobante_" + id + ".pdf").build());
        headers.setContentLength(pdf.length);
        return ResponseEntity.ok().headers(headers).body(pdf);
    }
}
