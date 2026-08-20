package com.cibertec.Proyecto_Integrador.service.impl;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import com.cibertec.Proyecto_Integrador.exception.ResourceNotFoundException;
import com.cibertec.Proyecto_Integrador.exception.StorageException;
import com.cibertec.Proyecto_Integrador.service.StorageService;

/**
 * Almacenamiento en disco local, bajo {@code app.uploads.dir}.
 *
 * <p>El nombre del archivo lo genera el servidor (UUID + extensión derivada del MIME
 * ya validado por ImagenProductoServiceImpl). El nombre original del cliente NUNCA se
 * usa: es entrada no confiable y es el vector clásico de path traversal.
 */
@Service
public class StorageServiceImpl implements StorageService {

    /** El MIME ya viene validado por el caller; este mapa sólo elige la extensión. */
    private static final Map<String, String> EXTENSION_BY_TYPE = Map.of(
            "image/jpeg", ".jpg",
            "image/png", ".png",
            "image/webp", ".webp");

    private final Path root;

    public StorageServiceImpl(@Value("${app.uploads.dir:./uploads}") String uploadsDir) {
        this.root = Paths.get(uploadsDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(root);
        } catch (IOException e) {
            throw new StorageException("No se pudo crear el directorio de uploads: " + root, e);
        }
    }

    @Override
    public String store(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new StorageException("El archivo está vacío");
        }
        String extension = EXTENSION_BY_TYPE.get(file.getContentType());
        if (extension == null) {
            throw new StorageException("Tipo de archivo no soportado: " + file.getContentType());
        }

        String filename = UUID.randomUUID() + extension;
        Path target = resolve(filename);

        try (InputStream in = file.getInputStream()) {
            Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new StorageException("No se pudo guardar el archivo: " + filename, e);
        }
        return filename;
    }

    @Override
    public Resource load(String filename) {
        Path target = resolve(filename);
        try {
            Resource resource = new UrlResource(target.toUri());
            if (!resource.exists() || !resource.isReadable()) {
                throw new ResourceNotFoundException("Imagen no encontrada: " + filename);
            }
            return resource;
        } catch (MalformedURLException e) {
            throw new StorageException("Ruta inválida para el archivo: " + filename, e);
        }
    }

    @Override
    public void delete(String filename) {
        try {
            Files.deleteIfExists(resolve(filename));
        } catch (IOException e) {
            throw new StorageException("No se pudo borrar el archivo: " + filename, e);
        }
    }

    /**
     * Resuelve el nombre contra la raíz y verifica que no se escape de ella.
     * Defensa en profundidad: hoy los nombres salen de la BD, pero un `../../` que
     * llegue por cualquier vía no debe poder leer ni borrar fuera de uploads.
     */
    private Path resolve(String filename) {
        if (filename == null || filename.isBlank()) {
            throw new StorageException("Nombre de archivo vacío");
        }
        Path resolved = root.resolve(filename).normalize();
        if (!resolved.startsWith(root)) {
            throw new StorageException("Nombre de archivo inválido: " + filename);
        }
        return resolved;
    }
}
