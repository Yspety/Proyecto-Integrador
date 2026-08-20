/**
 * Tipos del contrato de productos y categorías. Reflejan los DTOs del backend
 * (Jackson camelCase). Identificadores en inglés (contrato JSON).
 */

export interface CategoryResponse {
  id: number;
  name: string;
  description: string | null;
}

/** Body para crear/editar una categoría (POST/PUT /api/admin/categories). */
export interface CategoryRequest {
  name: string;
  description?: string | null;
}

/** Imagen del carrusel de un producto. */
export interface ProductImageResponse {
  id: number;
  /** URL completa de servicio: base-url + /api/uploads/images/{filename} */
  url: string;
  displayOrder: number;
  cover: boolean;
}

export interface ProductResponse {
  id: number;
  sku: string;
  name: string;
  description: string | null;
  /** Java BigDecimal → number. */
  price: number;
  stock: number;
  /** Punto de reposición: con stock <= stockMin el producto entra en alerta. */
  stockMin: number;
  imageUrl: string | null;
  active: boolean;
  categoryId: number;
  categoryName: string;
  /** Galería ordenada por displayOrder ASC. Ausente en endpoints de lista. */
  images?: ProductImageResponse[];
}

/** Body para crear/editar un producto (POST/PUT /api/admin/products). Refleja ProductRequest. */
export interface ProductRequest {
  sku: string;
  name: string;
  description?: string | null;
  price: number;
  stock: number;
  /** Opcional: si no viaja, el backend usa su default en el alta y conserva el valor en la edición. */
  stockMin?: number;
  imageUrl?: string | null;
  categoryId: number;
}

// ─── Inventario ───────────────────────────────────────────────────────────────

/** Un producto que llegó a su punto de reposición. */
export interface ProductoPorReponerRow {
  productId: number;
  sku: string;
  name: string;
  categoryName: string;
  stock: number;
  stockMin: number;
  /** Cuánto falta para volver al mínimo (stockMin − stock). */
  faltante: number;
}

/** Respuesta de GET /api/admin/inventory/low-stock. */
export interface AlertaStockResponse {
  total: number;
  /** Cuántos de esos están en CERO — esos ya están perdiendo ventas. */
  sinStock: number;
  productos: ProductoPorReponerRow[];
}

/** Envoltorio de paginación genérico — refleja PageResponse<T> de Spring Data. */
export interface PageResponse<T> {
  content: T[];
  /** Página base-cero. */
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

/** Estado del filtro del catálogo (búsqueda). */
export interface CatalogFilter {
  name?: string;
  categoryId?: number;
  priceMin?: number;
  priceMax?: number;
}

/** Imagen de respaldo cuando ProductResponse.imageUrl es null. */
export const PLACEHOLDER_IMAGE = '/brand/Krypton-navy.svg';
