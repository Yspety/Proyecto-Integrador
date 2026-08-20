import { useCallback, useEffect, useState } from 'react';
import { AlertTriangle, Images, Pencil, Plus, Search, Trash2 } from 'lucide-react';
import { listCategories, search } from '../catalog/products.api';
import { deleteProduct } from './admin-products.api';
import { getLowStock } from './admin-inventory.api';
import { ProductFormModal } from './ProductFormModal';
import { ProductImagesModal } from './ProductImagesModal';
import { PLACEHOLDER_IMAGE, type AlertaStockResponse, type CategoryResponse, type ProductResponse } from '../../models/product';
import './admin.css';

const pen = new Intl.NumberFormat('es-PE', { style: 'currency', currency: 'PEN', minimumFractionDigits: 2 });
const PAGE_SIZE = 10;

/** Panel de administración de productos: tabla + CRUD + galería de imágenes. */
export function AdminProductsPage() {
  const [products, setProducts] = useState<ProductResponse[]>([]);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [total, setTotal] = useState(0);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(false);

  // Filtros: buscador (debounced) + categoría. El backend ya soporta name/categoryId.
  const [nameQuery, setNameQuery] = useState('');
  const [nameDebounced, setNameDebounced] = useState('');
  const [categoryFilter, setCategoryFilter] = useState<number | ''>('');
  const [categories, setCategories] = useState<CategoryResponse[]>([]);

  // editing: null cerrado · 'new' alta · ProductResponse edición. imagesFor: galería.
  const [editing, setEditing] = useState<ProductResponse | 'new' | null>(null);
  const [imagesFor, setImagesFor] = useState<ProductResponse | null>(null);
  const [confirmDelete, setConfirmDelete] = useState<number | null>(null);

  // Alerta de reposición. Se pide entera (no paginada) porque el aviso tiene que
  // reflejar TODO el catálogo, no sólo lo que se ve en la página actual.
  const [lowStock, setLowStock] = useState<AlertaStockResponse | null>(null);
  const [showLowStock, setShowLowStock] = useState(false);

  // La alerta se recalcula en cada reload porque editar stock la cambia al instante.
  const reloadLowStock = useCallback(() => {
    getLowStock().then(setLowStock).catch(() => setLowStock(null));
  }, []);

  const reload = useCallback(() => {
    setLoading(true);
    setError(false);
    search({ name: nameDebounced || undefined, categoryId: categoryFilter || undefined }, page, PAGE_SIZE)
      .then((res) => { setProducts(res.content); setTotalPages(res.totalPages); setTotal(res.totalElements); })
      .catch(() => setError(true))
      .finally(() => setLoading(false));
    reloadLowStock();
  }, [page, nameDebounced, categoryFilter, reloadLowStock]);

  useEffect(() => { reload(); }, [reload]);

  // Buscador con debounce: un fetch al dejar de tipear (no uno por tecla).
  useEffect(() => {
    const t = setTimeout(() => setNameDebounced(nameQuery), 350);
    return () => clearTimeout(t);
  }, [nameQuery]);
  // Volver a página 0 al cambiar filtros + cargar categorías para el dropdown.
  useEffect(() => { setPage(0); }, [nameDebounced, categoryFilter]);
  useEffect(() => { listCategories().then(setCategories).catch(() => {}); }, []);

  const onDelete = async (id: number) => {
    try {
      await deleteProduct(id);
      setConfirmDelete(null);
      // Si era el único de la página, retroceder; si no, recargar la actual.
      if (products.length === 1 && page > 0) setPage((p) => p - 1);
      else reload();
    } catch {
      setError(true);
      setConfirmDelete(null);
    }
  };

  return (
    <div className="adm">
      <header className="adm-head">
        <div>
          <h1>Productos</h1>
          <p className="adm-sub">{loading ? 'Cargando…' : `${total} ${total === 1 ? 'producto' : 'productos'}`}</p>
        </div>
        <button type="button" className="adm-new" onClick={() => setEditing('new')}><Plus size={18} /> Nuevo producto</button>
      </header>

      <div className="adm-filters">
        <div className="adm-search">
          <Search size={16} />
          <input value={nameQuery} onChange={(e) => setNameQuery(e.target.value)} placeholder="Buscar por nombre…" />
        </div>
        <select className="adm-filter-sel" value={categoryFilter} onChange={(e) => setCategoryFilter(e.target.value ? Number(e.target.value) : '')}>
          <option value="">Todas las categorías</option>
          {categories.map((c) => <option key={c.id} value={c.id}>{c.name}</option>)}
        </select>
      </div>

      {error && <p className="adm-alert">Ocurrió un error con la operación. Reintentá.</p>}

      {lowStock && lowStock.total > 0 && (
        <section className="inv-alert">
          <button type="button" className="inv-alert__head" onClick={() => setShowLowStock((v) => !v)} aria-expanded={showLowStock}>
            <AlertTriangle size={18} />
            <span className="inv-alert__title">
              {lowStock.total} {lowStock.total === 1 ? 'producto necesita' : 'productos necesitan'} reposición
              {lowStock.sinStock > 0 && <em className="inv-alert__out"> · {lowStock.sinStock} sin stock</em>}
            </span>
            <span className="inv-alert__toggle">{showLowStock ? 'Ocultar' : 'Ver detalle'}</span>
          </button>

          {showLowStock && (
            <div className="adm-tablewrap">
              <table className="adm-table">
                <thead><tr><th>SKU</th><th>Producto</th><th>Categoría</th><th>Stock</th><th>Mínimo</th><th>Faltante</th></tr></thead>
                <tbody>
                  {lowStock.productos.map((p) => (
                    <tr key={p.productId}>
                      <td className="adm-sku">{p.sku}</td>
                      <td className="adm-name">{p.name}</td>
                      <td>{p.categoryName}</td>
                      <td><span className={p.stock === 0 ? 'adm-stock adm-stock--out' : 'adm-stock adm-stock--low'}>{p.stock}</span></td>
                      <td>{p.stockMin}</td>
                      <td><strong>+{p.faltante}</strong></td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </section>
      )}

      <div className="adm-tablewrap">
        <table className="adm-table">
          <thead>
            <tr><th aria-label="Imagen"></th><th>SKU</th><th>Nombre</th><th>Categoría</th><th>Precio</th><th>Stock</th><th>Mín.</th><th aria-label="Acciones"></th></tr>
          </thead>
          <tbody>
            {products.map((p) => (
              <tr key={p.id}>
                <td><img className="adm-thumb" src={p.imageUrl || PLACEHOLDER_IMAGE} alt="" /></td>
                <td className="adm-sku">{p.sku}</td>
                <td className="adm-name">{p.name}</td>
                <td>{p.categoryName}</td>
                <td>{pen.format(p.price)}</td>
                <td>
                  <span className={
                    p.stock === 0 ? 'adm-stock adm-stock--out'
                    : p.stock <= p.stockMin ? 'adm-stock adm-stock--low'
                    : 'adm-stock'
                  }>
                    {p.stock}
                  </span>
                  {p.stock > 0 && p.stock <= p.stockMin && (
                    <AlertTriangle className="adm-stock__warn" size={14} aria-label="Bajo el mínimo" />
                  )}
                </td>
                <td className="adm-stockmin">{p.stockMin}</td>
                <td className="adm-actions">
                  {confirmDelete === p.id ? (
                    <span className="adm-confirm">
                      ¿Borrar?
                      <button type="button" className="adm-confirm__yes" onClick={() => onDelete(p.id)}>Sí</button>
                      <button type="button" className="adm-confirm__no" onClick={() => setConfirmDelete(null)}>No</button>
                    </span>
                  ) : (
                    <>
                      <button type="button" title="Imágenes" onClick={() => setImagesFor(p)}><Images size={17} /></button>
                      <button type="button" title="Editar" onClick={() => setEditing(p)}><Pencil size={17} /></button>
                      <button type="button" title="Borrar" className="adm-del" onClick={() => setConfirmDelete(p.id)}><Trash2 size={17} /></button>
                    </>
                  )}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
        {!loading && products.length === 0 && <p className="adm-empty">No hay productos.</p>}
      </div>

      {totalPages > 1 && (
        <nav className="adm-pages" aria-label="Paginación">
          <button type="button" disabled={page === 0} onClick={() => setPage((p) => p - 1)}>Anterior</button>
          <span>Página {page + 1} de {totalPages}</span>
          <button type="button" disabled={page >= totalPages - 1} onClick={() => setPage((p) => p + 1)}>Siguiente</button>
        </nav>
      )}

      {editing && (
        <ProductFormModal
          product={editing === 'new' ? null : editing}
          onClose={() => setEditing(null)}
          onSaved={() => { setEditing(null); reload(); }}
        />
      )}
      {imagesFor && (
        <ProductImagesModal product={imagesFor} onClose={() => setImagesFor(null)} onChanged={reload} />
      )}
    </div>
  );
}
