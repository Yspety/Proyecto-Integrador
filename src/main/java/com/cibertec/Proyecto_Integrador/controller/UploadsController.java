package com.cibertec.Proyecto_Integrador.controller;

import java.time.Duration;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.cibertec.Proyecto_Integrador.service.StorageService;

/**
 * Sirve los binarios subidos. Es la contraparte de las URLs que arma ProductoMapper
 * ({@code app.uploads.base-url + /api/uploads/images/{filename}}): sin este endpoint,
 * cada imagen de producto responde 404.
 *
 * <p>Público (permitAll en SecurityConfig): las fotos del catálogo se ven sin login.
 */
@RestController
@RequestMapping("/api/uploads/images")
public class UploadsController {

    private final StorageService storageService;

    public UploadsController(StorageService storageService) {
        this.storageService = storageService;
    }

    /**
     * GET /api/uploads/images/{filename} → 200 con el binario. 404 si no existe.
     *
     * <p>Cacheable de forma agresiva porque el nombre es un UUID: el contenido de un
     * filename dado nunca cambia. Reemplazar una imagen genera un nombre nuevo.
     */
    @GetMapping("/{filename}")
    public ResponseEntity<Resource> serve(@PathVariable String filename) {
        Resource file = storageService.load(filename);

        MediaType contentType = MediaTypeFactory.getMediaType(file)
                .orElse(MediaType.APPLICATION_OCTET_STREAM);

        return ResponseEntity.ok()
                .contentType(contentType)
                .cacheControl(CacheControl.maxAge(Duration.ofDays(365)).cachePublic().immutable())
                .body(file);
    }
}
