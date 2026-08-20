package com.cibertec.Proyecto_Integrador.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import com.cibertec.Proyecto_Integrador.dto.request.ItemCarritoRequest;
import com.cibertec.Proyecto_Integrador.dto.request.UpdateQuantityRequest;
import com.cibertec.Proyecto_Integrador.dto.response.CarritoResponse;
import com.cibertec.Proyecto_Integrador.service.CarritoService;

/**
 * Carrito del usuario autenticado. Requiere token ({@code anyRequest().authenticated()}).
 *
 * <p>El carrito es SIEMPRE el del que hace la request: el email sale del JWT, nunca de
 * un parámetro. Por eso no hay ningún {@code userId} en las rutas — no existe forma de
 * pedir el carrito de otro. Los ids de ítem sí viajan en el path, y el service verifica
 * que la línea pertenezca al usuario antes de tocarla (responde 404, no 403, para no
 * revelar si el id existe).
 *
 * <p>{@code @AuthenticationPrincipal String email} funciona porque JwtAuthFilter deja el
 * email como principal.
 */
@RestController
@RequestMapping("/api/cart")
public class CarritoController {

    private final CarritoService cartService;

    public CarritoController(CarritoService cartService) {
        this.cartService = cartService;
    }

    /**
     * GET /api/cart → 200 CarritoResponse.
     * Si el usuario nunca agregó nada devuelve un carrito vacío sintético,
     * sin crear la fila: mirar el carrito no debería escribir en la base.
     */
    @GetMapping
    public CarritoResponse obtenerCarrito(@AuthenticationPrincipal String email) {
        return cartService.obtenerCarrito(email);
    }

    /**
     * POST /api/cart/items → 200 CarritoResponse con el carrito completo ya actualizado.
     * Si el producto ya estaba, SUMA la cantidad. 404 si el producto no existe o está
     * inactivo; 409 si la cantidad final supera el stock.
     *
     * <p>Llama a {@code agregarItem}, NUNCA a intentarAgregarItem/fusionarEnConflicto:
     * esos dos son públicos sólo para que el proxy de Spring pueda abrir una transacción
     * en cada uno. Invocarlos desde acá saltearía el reintento ante colisión.
     */
    @PostMapping("/items")
    public CarritoResponse agregarItem(@AuthenticationPrincipal String email,
                                       @Valid @RequestBody ItemCarritoRequest request) {
        return cartService.agregarItem(email, request);
    }

    /**
     * PUT /api/cart/items/{itemId} → 200 CarritoResponse.
     * FIJA la cantidad (no suma). Para llevarla a cero se usa DELETE, no quantity=0:
     * el request valida @Min(1).
     */
    @PutMapping("/items/{itemId}")
    public CarritoResponse actualizarItem(@AuthenticationPrincipal String email,
                                          @PathVariable Long itemId,
                                          @Valid @RequestBody UpdateQuantityRequest request) {
        return cartService.actualizarItem(email, itemId, request);
    }

    /** DELETE /api/cart/items/{itemId} → 204. 404 si la línea no es del usuario. */
    @DeleteMapping("/items/{itemId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void quitarItem(@AuthenticationPrincipal String email, @PathVariable Long itemId) {
        cartService.quitarItem(email, itemId);
    }

    /** DELETE /api/cart → 204. Idempotente: vaciar un carrito ya vacío no falla. */
    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void vaciarCarrito(@AuthenticationPrincipal String email) {
        cartService.vaciarCarrito(email);
    }
}
