import { api } from '../../lib/api';
import type { AlertaStockResponse } from '../../models/product';

/** GET /api/admin/inventory/low-stock — productos en su punto de reposición o por debajo. */
export async function getLowStock(): Promise<AlertaStockResponse> {
  const { data } = await api.get<AlertaStockResponse>('/api/admin/inventory/low-stock');
  return data;
}
