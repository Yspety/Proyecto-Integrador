package com.cibertec.Proyecto_Integrador.service.impl;

import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.cibertec.Proyecto_Integrador.dto.request.CategoriaRequest;
import com.cibertec.Proyecto_Integrador.dto.response.CategoriaResponse;
import com.cibertec.Proyecto_Integrador.entity.Categoria;
import com.cibertec.Proyecto_Integrador.exception.CategoryInUseException;
import com.cibertec.Proyecto_Integrador.exception.DuplicateCategoryNameException;
import com.cibertec.Proyecto_Integrador.exception.ResourceNotFoundException;
import com.cibertec.Proyecto_Integrador.mapper.CategoriaMapper;
import com.cibertec.Proyecto_Integrador.repository.CategoriaRepository;
import com.cibertec.Proyecto_Integrador.repository.ProductoRepository;
import com.cibertec.Proyecto_Integrador.service.CategoriaService;

@Service
public class CategoriaServiceImpl extends ICRUDImpl<Categoria, Long> implements CategoriaService {

    private final CategoriaRepository categoryRepository;
    private final ProductoRepository productRepository;
    private final CategoriaMapper categoryMapper;

    public CategoriaServiceImpl(CategoriaRepository categoryRepository,
                                ProductoRepository productRepository,
                                CategoriaMapper categoryMapper) {
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
        this.categoryMapper = categoryMapper;
    }

    /** Repository que usa el CRUD genérico heredado (guardar/listarTodos/...). */
    @Override
    protected JpaRepository<Categoria, Long> repo() {
        return categoryRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoriaResponse> listar() {
        // No usamos el listarTodos() genérico: devuelve sin orden y el filtro del
        // catálogo necesita las categorías alfabéticas para ser usable.
        return categoryRepository.findAll(Sort.by(Sort.Direction.ASC, "name")).stream()
                .map(categoryMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public CategoriaResponse registrar(CategoriaRequest request) {
        if (categoryRepository.existsByName(request.name())) {
            throw new DuplicateCategoryNameException("Ya existe una categoría con ese nombre: " + request.name());
        }
        Categoria category = new Categoria();
        category.setName(request.name());
        category.setDescription(request.description());
        return categoryMapper.toResponse(guardar(category));   // ← heredado de ICRUDImpl
    }

    @Override
    @Transactional
    public CategoriaResponse actualizar(Long id, CategoriaRequest request) {
        Categoria category = obtenerPorId(id);   // ← heredado: ya lanza 404 si no existe

        if (categoryRepository.existsByNameAndIdNot(request.name(), id)) {
            throw new DuplicateCategoryNameException("Ya existe otra categoría con ese nombre: " + request.name());
        }
        category.setName(request.name());
        category.setDescription(request.description());
        return categoryMapper.toResponse(guardar(category));   // ← heredado de ICRUDImpl
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        if (!categoryRepository.existsById(id)) {
            throw new ResourceNotFoundException("Categoría no encontrada: " + id);
        }
        // Se comprueba ANTES de borrar: Producto.category es optional=false, así que
        // dejar productos huérfanos rompería el catálogo entero. Y como Producto usa
        // soft-delete, un producto inactivo sigue contando — su fila sigue ahí.
        if (productRepository.existsByCategoryId(id)) {
            throw new CategoryInUseException("La categoría tiene productos asociados");
        }
        borrar(id);   // ← heredado de ICRUDImpl (hard delete: Categoria no tiene flag active)
    }
}
