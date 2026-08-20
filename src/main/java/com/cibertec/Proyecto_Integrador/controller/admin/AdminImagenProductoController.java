package com.cibertec.Proyecto_Integrador.controller.admin;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import com.cibertec.Proyecto_Integrador.service.ImagenProductoService;

/**
 * Galería de imágenes de un producto — sólo ADMIN.
 *
 * <p>Sub-recurso anidado bajo el producto: el {@code productId} del path es siempre
 * el dueño, y el service valida que la imagen le pertenezca.
 *
 * <p>Ninguna operación devuelve cuerpo (204). El front vuelve a pedir el detalle del
 * producto para refrescar la galería, así que no hay dos formas de armar la lista.
 */
@RestController
@RequestMapping("/api/admin/products/{productId}/images")
public class AdminImagenProductoController {

    private final ImagenProductoService productImageService;

    public AdminImagenProductoController(ImagenProductoService productImageService) {
        this.productImageService = productImageService;
    }

    /**
     * POST .../images → 204. Multipart, campo {@code file}.
     * Valida tipo (jpeg/png/webp), tamaño (5 MB) y máximo de 10 imágenes por producto.
     * La primera imagen del producto queda como portada automáticamente.
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void subir(@PathVariable Long productId, @RequestParam("file") MultipartFile file) {
        productImageService.subir(productId, file);
    }

    /** DELETE .../images/{imageId} → 204. Si borrás la portada, promueve la siguiente. */
    @DeleteMapping("/{imageId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long productId, @PathVariable Long imageId) {
        productImageService.eliminar(productId, imageId);
    }

    /**
     * PATCH .../images/reorder → 204. Body = array plano de ids en el nuevo orden.
     * ESTRICTO Y COMPLETO: tiene que traer exactamente los ids del producto, ni uno
     * de más ni uno de menos. Cualquier otra cosa es 400.
     */
    @PatchMapping("/reorder")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void reordenar(@PathVariable Long productId, @RequestBody List<Long> orderedIds) {
        productImageService.reordenar(productId, orderedIds);
    }

    /** PATCH .../images/{imageId}/cover → 204. Idempotente si ya es la portada. */
    @PatchMapping("/{imageId}/cover")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void definirPortada(@PathVariable Long productId, @PathVariable Long imageId) {
        productImageService.definirPortada(productId, imageId);
    }
}
