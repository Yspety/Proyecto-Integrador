# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## What this is

Monorepo for **Krypton**, an e-commerce built as a CIBERTEC integrator project:

- **Backend** (`src/`, `pom.xml`) — Spring Boot 4.1, Java 21, Spring Data JPA + MySQL, Spring Security with JWT (jjwt 0.12.6), Lombok.
- **Frontend** (`frontend/`) — React 19 + TypeScript + Vite 8, react-router 7, axios, recharts, lucide-react. Plain CSS, no UI framework.

The two halves talk over `http://localhost:8080` by default (`frontend/src/config.ts`, overridable with `VITE_API_BASE_URL`).

## Current state — read this before assuming anything works

Both halves **boot**. The backend starts on `:8080` against MySQL, and Vite serves the frontend on `:5173`. What is missing is not infrastructure any more — it is **endpoints**.

Working end to end: **auth** (`/api/auth/**`), **user admin** (`/api/admin/users`), the whole **catalog** — public product search/detail, categories, admin CRUD for both, the product image gallery (upload/reorder/cover/delete) and binary serving at `/api/uploads/images/**` — the **cart** (`/api/cart*`), and **orders** (`/api/orders*`, `/api/admin/orders*`) including checkout, simulated payment, the status machine, stock/kardex bookkeeping and the PDF comprobante.

The backend is being built **module by module**, and each module lands as *DTOs + service + mapper* first, with controllers and wiring later. Still absent:

- **Promos and reviews** exist only in the frontend — no entity, service or controller for either. `CheckoutRequest.couponCode` is sent by the frontend but the backend DTO has no such field, so `OrdenResponse.discount` is always `0.00` until the promo module exists.
Everything else the frontend calls is implemented.

### First ADMIN

Public registration hardcodes `Rol.CLIENTE` and `/api/admin/users` requires `ROLE_ADMIN`, so the panel would be unreachable on a fresh database. `config/AdminSeeder` breaks that cycle: on startup, **only if no active ADMIN exists**, it creates one from `app.seed.admin.*` (defaults `admin@krypton.pe` / `Krypton.Admin.2026`) and logs a WARN with the credentials. If that email already exists as a CLIENTE it promotes that row instead of failing on the unique constraint.

It is idempotent — a restart with an admin present is a no-op. Turn it off with `app.seed.admin.enabled=false`, and override the password via `ADMIN_PASSWORD` in anything resembling a real deployment.

### Local setup

MySQL must be running on `localhost:3306`. The JDBC URL uses `createDatabaseIfNotExist=true`, so the `krypton` schema is created on first boot and Hibernate builds the tables via `ddl-auto=update` — there is no `.sql` script in the repo.

Every credential and URL in `application.properties` is overridable by env var: `DB_USER`, `DB_PASSWORD`, `JWT_SECRET`, `UPLOADS_DIR`, `UPLOADS_BASE_URL`, `CORS_ORIGINS`, `ADMIN_SEED_ENABLED`, `ADMIN_EMAIL`, `ADMIN_NAME`, `ADMIN_PASSWORD`. The committed defaults are **dev-only** — `app.jwt.secret` in particular must be overridden in any real deployment, and HS256 requires it to be at least 32 characters.

## Commands

Backend (PowerShell — use `./mvnw` from the Bash tool):

```bash
./mvnw spring-boot:run
```

```bash
./mvnw test
```

Single test class or method:

```bash
./mvnw test -Dtest=ProyectoIntegradorApplicationTests#contextLoads
```

Frontend (from `frontend/`):

```bash
npm run dev
```

```bash
npm run lint
```

`npm run build` runs `tsc -b && vite build` — that is the only way to get real type checking, since ESLint here is not type-aware.

## Backend architecture

Layered, strictly one direction: `controller → service (interface) → service.impl → repository → entity`, with `mapper` translating entity → DTO. **Entities never leave the service layer.**

### The generic CRUD base

`service/ICRUD<T, ID>` + `service/impl/ICRUDImpl<T, ID>` give every service `guardar` / `borrar` / `listarTodos` / `obtenerPorId` at the **entity** level. Concrete impls extend `ICRUDImpl`, implement `repo()` to hand it their repository, and use the inherited methods *internally*. The generic method names are deliberately different from the public domain API (`registrar` / `actualizar` / `eliminar` / `buscarPorId`) so they never collide. Missing rows throw `ResourceNotFoundException`, not a checked exception.

Domain logic overrides the generic when it differs — e.g. `ProductoServiceImpl.eliminar()` does a **soft delete** (`active = false`) instead of calling the inherited `borrar()`.

### Naming convention (important, easy to get wrong)

- **Java identifiers are Spanish**: `Producto`, `Usuario`, `Carrito`, `OrdenService`, `registrar`, `buscarPorId`.
- **The JSON contract is English**: entity *columns/fields* (`name`, `price`, `stock`, `active`), DTO record components (`ProductoResponse(id, sku, name, ...)`), and REST paths (`/api/products`, `/api/admin/orders`).
- Inside impls, injected beans are named in English (`productRepository`, `cartMapper`) even though their types are Spanish.

Keep both sides of this split when adding code — the frontend types in `frontend/src/models/` mirror the English contract exactly.

### DTOs, paging and filtering

- Requests and responses are **Java records** in `dto/request` and `dto/response`, validated with `@Valid` + Jakarta Validation.
- Paginated endpoints return `PageResponse<T>` (`content`, `page`, `size`, `totalElements`, `totalPages`), built with `PageResponse.of(page)` from an *already-mapped* `Page<DTO>`. Spring's `Page` is never serialized directly.
- Dynamic filters use JPA Specifications composed in the service — see `ProductoSpecification` + `ProductoServiceImpl.buscar()`, which always ANDs `isActive(true)` first. Absent filters return `Specification.unrestricted()`, **never `null`**: Spring Data JPA 4 rejects `and(null)` with `IllegalArgumentException: Other specification must not be null`. Any new Specification factory must follow that.

### Mappers

`@Component` classes in `mapper/`, hand-written (no MapStruct). `ProductoMapper` has two modes on purpose: `toResponse()` is lean (`images = null`, dropped from JSON) for lists, `toResponseWithImages()` is full for detail. The full mode touches a **LAZY** collection, so its caller must stay inside `@Transactional` — `ProductoServiceImpl.buscarPorId()` is annotated for exactly that reason.

### Security model

Stateless JWT HS256 (`security/JwtService`): subject = user email, custom `role` claim, symmetric key from `app.jwt.secret`. Nothing is stored server-side.

`security/JwtAuthFilter` turns the `Authorization: Bearer` header into an `Authentication` whose **principal is the email string** and whose authority is `ROLE_` + the `role` claim — no DB lookup. That principal type is deliberate: domain services take the email as their first parameter, so controllers can pass `Authentication#getName()` (or `@AuthenticationPrincipal String email`) straight through.

The filter is **not** a Spring bean, on purpose: Boot auto-registers any `Filter` bean into the servlet container's chain, where it would run a second time outside Security. `SecurityConfig` constructs it by hand instead.

`config/SecurityConfig` holds the `PasswordEncoder` (BCrypt) bean, the route rules, and the CORS source (`app.cors.allowed-origins`, default `http://localhost:5173`). Public routes: `/api/auth/**`, `GET /api/products/**`, `GET /api/categories/**`, `GET /api/uploads/**`. `/api/admin/**` requires `ROLE_ADMIN`; everything else requires authentication.

**`ERROR` and `ASYNC` dispatches are explicitly permitted** — do not remove that line. Since Spring Security 6 the chain also runs on the error dispatch, so an unhandled exception gets re-dispatched to `/error` anonymously and comes back as **401**. A real 500 then looks like an auth problem, and you lose hours chasing a token that was never wrong. This actually happened while building the image gallery: a `DataIntegrityViolationException` surfaced as a 401 on upload.

Ownership-scoped endpoints take the **email** as their first parameter (`obtenerCarrito(String email, ...)`, `miOrden(String email, Long id)`) and resolve the user themselves; a resource that belongs to someone else throws `ResourceNotFoundException` (404, not 403) so IDOR probes cannot enumerate. Admin endpoints under `/api/admin/**` are *not* ownership-scoped and take no email.

`AuthServiceImpl.login()` returns the same `InvalidCredentialsException` message for unknown email, wrong password, and inactive account — do not make these distinguishable.

### Transaction gotcha worth knowing

`CarritoServiceImpl.agregarItem()` is deliberately **not** `@Transactional`. It is an orchestrator that calls `self.intentarAgregarItem()` (tx1) and, on `DataIntegrityViolationException` from the UNIQUE(cart, product) constraint, retries with `self.fusionarEnConflicto()` (tx2). `self` is a `@Lazy`-injected proxy of the service's own interface — calling `this.` would bypass the proxy and both would run in the same doomed transaction. Preserve this shape if you touch the cart.

That whole mechanism is load-bearing on `UNIQUE(cart_id, product_id)`. `ItemCarrito` declares it via `@UniqueConstraint(name = "uk_cart_item_cart_product", ...)`, and `ddl-auto=update` applies it to an existing table (verified with `SHOW INDEX FROM cart_item`: `Non_unique=0` over `cart_id`, `product_id`).

Do not drop that declaration. Without the constraint the insert never collides, so `fusionarEnConflicto` becomes dead code, and two concurrent adds of the same product write two rows — the product then shows up twice in the cart.

To check it on a fresh machine:

```sql
SHOW INDEX FROM cart_item WHERE Key_name = 'uk_cart_item_cart_product';
```

Also: editing a product's `stock` through `ProductoServiceImpl.actualizar()` is applied as an **inventory adjustment** — `KardexService.ajustar()` writes an ENTRADA or SALIDA movement for the delta and then moves the balance. It reads the row with `findByIdWithLock` because that is a read-modify-write against the same stock a checkout may be decrementing. No change means no movement, so the kardex does not fill with noise.

## Orders, stock and money

`OrdenServiceImpl.confirmarCompra()` is the only place stock goes down, and it does everything in one transaction: decrement stock, snapshot the lines, write the kardex, empty the cart. A failure anywhere rolls the whole thing back.

Two rules that are easy to break and expensive to debug:

- **Products are locked in ascending id order** via `ProductoRepository.findByIdWithLock` (`PESSIMISTIC_WRITE`). The lock stops two buyers from both reading "1 left" and both decrementing. The *ordering* stops two concurrent checkouts sharing products from deadlocking against each other. Keep both.
- **`Producto.stock` never changes without a `MovimientoStock` row explaining it.** The kardex is append-only: a mistake is corrected with the opposite movement, never by editing or deleting a row. `stock` is the balance; the kardex is how it got there, and reconciliation (`SUM(entradas - salidas)`) has to keep matching. Every writer goes through `KardexService` — checkout, cancellation and admin inventory adjustments alike. Do not write `MovimientoStock` or mutate `stock` anywhere else.

Money: catalog prices **already include IGV**, so tax is broken out *inward* — `base = total / 1.18`, `igv = total - base`. Subtracting rather than multiplying the base by the rate guarantees `base + igv == total` with no one-cent drift. Shipping is free from S/300, else S/20. All of it is persisted on the order rather than recomputed on read, because an issued comprobante must not change when the IGV rate or shipping policy does. Tunable via `app.orders.{igv-rate,free-shipping-from,shipping-cost}`.

`EstadoOrdenPolicy` is the single source of truth for legal status transitions and for whether an order counts as paid (which gates the comprobante). Illegal transitions are **422**, not 400 — the request is well formed, the jump is not. There are no self-transitions: re-sending the current status is a client error, not a silent no-op.

The comprobante PDF is drawn with PDFBox in `ComprobanteGeneratorPdfBox`. All variable text goes through its `sanitize()` because the Standard-14 fonts use WinAnsiEncoding — one product name with a typographic dash would otherwise throw and break the whole download.

## Frontend architecture

Feature-sliced under `frontend/src/features/<feature>/`. Each feature owns its pages (`*.tsx`), its HTTP layer (`*.api.ts`), and its styles (`*.css`), and imports shared types from `frontend/src/models/`.

- `lib/api.ts` — the single axios instance. A request interceptor attaches `Bearer` from `localStorage['token']`. **Always** go through this, never call axios directly.
- `lib/apiError.ts` — `apiErrorMessage(err, fallback)` pulls the backend's `error` field. Use it for every user-facing failure message.
- `auth/AuthContext.tsx` — decodes the JWT payload client-side (base64url, no signature check) purely to populate `{ email, role }` and drive UI. Expired token → cleared on mount. `RequireAdmin` is the route guard; real authorization is the backend's job.
- `cart/CartContext.tsx` — cart state shared across the app.
- Routing lives entirely in `App.tsx`, with two shells: bare for `/cuenta/**` (login/register), `MainLayout` (navbar + footer) for everything else, and `RequireAdmin > AdminLayout` nested under `/admin`. **Routes are in Spanish** (`/catalogo`, `/carrito`, `/pedidos`, `/admin/productos`).

### Styling

`src/index.css` imports the design system in order: fonts → colors → typography → spacing → base. `styles/design-system/colors.css` defines the Krypton brand ramps (`--kr-*`) **and** semantic aliases (`--color-brand`, `--surface-card`, `--text-body`, `--action-cta`, …). Product code must use the **semantic aliases**, never the raw `--kr-*` ramp values. Component styles are plain CSS files colocated with the component.

## API surface

Implemented and reachable:

- `POST /api/auth/{register,login}`
- `GET /api/products`, `GET /api/products/{id}`, `GET /api/categories` — public
- `GET /api/uploads/images/{filename}` — public, immutable cache headers (UUID filenames never change content)
- `GET/POST/PUT/DELETE /api/admin/products` (the GET includes inactive), `PATCH /api/admin/products/{id}/status`
- `POST/PUT/DELETE /api/admin/categories`
- `POST/DELETE/PATCH /api/admin/products/{id}/images*`
- `GET/POST/PATCH /api/admin/users*`
- `GET/POST/PUT/DELETE /api/cart*` — authenticated, scoped to the caller
- `POST /api/orders/checkout`, `GET /api/orders`, `GET /api/orders/{id}`, `POST /api/orders/{id}/pay`, `GET /api/orders/{id}/comprobante`
- `GET/PUT /api/admin/orders*` incl. `GET /api/admin/orders/{id}/comprobante`
- `GET /api/admin/reports/{ventas,productos-vendidos,kardex}`
- `GET /api/admin/inventory/low-stock`

### Inventory alerts

`Producto.stockMin` is the reorder point, **per product** rather than one global threshold: a cable that restocks overnight and an imported laptop with a three-week lead time cannot share a number, and an alert tuned wrong for half the catalog is an alert people stop reading. New products fall back to `app.inventory.default-stock-min` (5) when the request omits it.

`stockMin` is **optional** on `ProductoRequest`: `null` means "not touching it" — the default on create, the current value on update. Never treat it as zero.

`GET /api/admin/inventory/low-stock` returns active products with `stock <= stockMin`, worst first (`faltante` desc, then `stock` asc so a zero outranks an equal shortfall). Inactive products are excluded — you do not reorder something you discontinued. `sinStock` is broken out separately because zero stock is losing sales right now, not just approaching a threshold. It lives under `/inventory`, not `/reports`, because it is operational state to act on, not a historical report.

### Reports

Report DTOs use **Spanish field names** (`periodo`, `ordenes`, `monto`, `filas`, `nombre`, `unidades`, `ingresos`) — the one deliberate exception to the English-JSON rule, because `frontend/src/models/report.ts` already declared that contract.

Three rules the numbers depend on:

- **Only paid orders count** (`EstadoOrdenPolicy.estadosPagados()`). PENDIENTE is not money in yet and CANCELADA never was; counting them would inflate revenue with sales that did not happen.
- **Everything is grouped in `America/Lima`, not UTC.** A 21:00 sale in Lima is 02:00 UTC the next day — grouping by UTC files it under the wrong date and the report stops matching what the business saw.
- **Date ranges are inclusive on both ends.** `desde` maps to Lima midnight, `hasta` to Lima midnight of the *following* day (exclusive upper bound), so the last day counts in full.

`topProductos` groups by the **catalog** name, not the line snapshot, because the question is how a product sells today.

**Empty periods are asymmetric on purpose.** `ventasPorPeriodo` returns every bucket in the range, zero-filled — the line chart needs them, because joining two points across dead days draws a gradual trend that never happened. The **exports filter those rows out** (`ReporteExportServiceImpl.tablaVentas`) and say so in the subtitle: a missing table row implies nothing, while thirty zero rows bury the days that matter. Keep the zero-fill in the JSON and the filter in the export; do not "unify" them.

### Report exports

`GET /api/admin/reports/{ventas,productos-vendidos,kardex}/{excel,pdf}` take the **same query params** as their JSON counterparts and go through the same `ReporteService` — an export must never run its own query, or the spreadsheet will disagree with the dashboard it was downloaded from.

Three reports × two formats is not six hand-written exporters. Each report is flattened into a neutral `service/export/TablaReporte` (title, subtitle, headers, rows, KPIs, plus a per-column `numericas` flag), and `ExcelWriter` / `PdfWriter` render it. Adding CSV later means one more writer and no changes to the reports; adding a report means one more table builder.

- Excel writes numeric columns as **real numbers**, not text — an export you cannot sum or chart in Excel is barely an export.
- The PDF is **landscape** and repeats the header row on every page; a multi-page kardex where only page 1 labels the columns is useless on paper.
- `PdfWriter.sanitize()` exists for the same WinAnsiEncoding reason as `ComprobanteGeneratorPdfBox`.

Note when debugging PDF output: PDFBox writes strings containing non-ASCII as **hex** (`<4B61...> Tj`) and plain ASCII as literals (`(Checkout) Tj`). A text extractor that only scans `(...)` will silently drop every accented string and look like data loss that is not there.

Cart and order controllers take the owner via `@AuthenticationPrincipal String email` (works because `JwtAuthFilter` sets the email as the principal). No route carries a `userId` — there is no way to address someone else's cart or order.

Called by the frontend but **not yet implemented** — the remaining backlog: `/api/admin/promos`, `/api/promos/apply`, `/api/reviews`, `/api/admin/reports/{ventas,productos-vendidos,kardex}`.

### Public listing vs admin listing

`GET /api/products` always filters `active = true`; `GET /api/admin/products` does not, and takes an optional `active` param (`true` / `false` / omitted = both). `DELETE /api/admin/products/{id}` is a **soft delete**, so the admin listing is the only way to see a deleted product, and `PATCH /api/admin/products/{id}/status` brings it back.

The panel used to reuse the public search — one query instead of two — and that quietly made deleted products unrecoverable from the UI. It read as data loss: users assumed `ddl-auto=update` was wiping the database on restart. It never was. **A soft delete needs a listing that can see the deleted rows, or it is a hard delete with extra steps.**

### Error contract

Every failure returns `{ "error": "<mensaje>" }` — the `ApiError` record, which is the exact shape `frontend/src/lib/apiError.ts` reads. `GlobalExceptionHandler` maps domain exceptions (404 not-found, 401 bad-credentials, 409 conflicts, 400 validation, 413 upload-too-large); `SecurityConfig` emits the same shape for 401/403. There is deliberately **no** `@ExceptionHandler(Exception.class)` — it would swallow Spring's own 404/405 and report them as 500.

## Known cruft

- `spec/InvalidCredentialsException.java` is an empty dead stub; the real one is `exception/InvalidCredentialsException.java`. The `spec` package should only hold Specifications.
- `frontend/README.md` is the untouched Vite template and describes nothing about this project.
