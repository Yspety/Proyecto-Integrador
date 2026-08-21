# Krypton

E-commerce del Proyecto Integrador — CIBERTEC VI.

- **Backend**: Spring Boot 4.1 · Java 21 · MySQL · JWT
- **Frontend**: React 19 · TypeScript · Vite

---

## 1. Qué necesitás instalado

| | Versión | Cómo verificar |
|---|---|---|
| **JDK** | 21 | `java -version` |
| **Node.js** | 20 o superior | `node --version` |
| **MySQL** | 8.x, corriendo en `localhost:3306` | que el servicio esté iniciado |

Maven **no** hace falta instalarlo: el repo trae el wrapper (`mvnw`).

---

## 2. Configurar la base de datos

La base `krypton` se crea sola en el primer arranque (`createDatabaseIfNotExist=true`) y
Hibernate genera las tablas. **No hace falta crear nada a mano.**

Lo único que sí tenés que ajustar es **tu contraseña de MySQL**.

El archivo [`src/main/resources/application.properties`](src/main/resources/application.properties)
trae este default:

```properties
spring.datasource.username=${DB_USER:root}
spring.datasource.password=${DB_PASSWORD:mysql}
```

Si tu MySQL local usa otro usuario o contraseña — que es lo normal — **no edites el
archivo**: definí las variables de entorno antes de arrancar. Así cada uno usa las suyas
y no nos pisamos el archivo en cada commit.

PowerShell (Windows):

```powershell
$env:DB_USER="root"; $env:DB_PASSWORD="tu_password"
```

Bash (Git Bash, Linux, macOS):

```bash
export DB_USER=root DB_PASSWORD=tu_password
```

Esas variables valen solo para esa terminal. Si abrís una nueva, definilas otra vez (o
agregalas a las variables de entorno del sistema).

### Traer el catálogo del equipo

El repo trae un volcado con los datos de demo — categorías, productos, fotos, usuarios y
pedidos — para que todos trabajemos sobre lo mismo:

```bash
bash db/import.sh
```

> ⚠ Reemplaza tu base `krypton` local. Si ya cargaste cosas que querés conservar, corré
> antes `bash db/export.sh` y guardate el archivo.

Las fotos de los productos vienen en `uploads/` con el repo, así que no hay nada más
que hacer.

**Si cargás productos nuevos y querés compartirlos**, exportá y commiteá las dos cosas:

```bash
bash db/export.sh && git add db/krypton_seed.sql uploads/
```

El detalle está en [`db/README.md`](db/README.md).

---

## 3. Levantar el backend

Desde la **raíz** del proyecto:

```bash
./mvnw spring-boot:run
```

En PowerShell, si `./mvnw` no te funciona, usá `.\mvnw.cmd spring-boot:run`.

Queda escuchando en **http://localhost:8080**. Sabés que arrancó bien cuando ves:

```
Started ProyectoIntegradorApplication in 8.0 seconds
```

### El primer administrador

En el primer arranque, si la base no tiene ningún admin, se crea uno solo y lo avisa en
la consola con un recuadro:

```
┌──────────────────────────────────────────────────────────────┐
│  NO HABÍA NINGÚN ADMIN — se creó uno nuevo                   │
└──────────────────────────────────────────────────────────────┘
  email    : admin@krypton.pe
  password : Krypton.Admin.2026
```

Con esas credenciales entrás al panel de `/admin`. Es idempotente: si ya existe un admin,
no hace nada.

---

## 4. Levantar el frontend

En **otra terminal**, desde la carpeta `frontend/`:

```bash
npm install
```

```bash
npm run dev
```

Queda en **http://localhost:5173**. El `npm install` solo hace falta la primera vez (o
cuando alguien agrega una dependencia).

El frontend apunta a `http://localhost:8080` por defecto. Si necesitás cambiarlo, creá un
archivo `frontend/.env.local` con `VITE_API_BASE_URL=http://otro-host:puerto`.

---

## 5. Orden de arranque

1. **MySQL** — si no está corriendo, el backend no levanta.
2. **Backend** — `./mvnw spring-boot:run`
3. **Frontend** — `npm run dev`

El frontend levanta igual sin el backend, pero cada pantalla va a mostrar errores de
carga: no tiene de dónde traer los datos.

---

## 6. Probar que funciona

Entrá a http://localhost:5173 y recorré:

1. **Catálogo** → abrí un producto → **Agregar al carrito**
2. **Carrito** → **Finalizar compra** (te pide iniciar sesión; creá una cuenta)
3. Completá boleta con un DNI de 8 dígitos → **Confirmar pedido**
4. En el detalle del pedido → **Pagar** (es simulado, no cobra nada)
5. **Descargar boleta** → te baja el PDF

Y con el admin (`admin@krypton.pe`):

- `/admin/productos` — alta, edición, imágenes y alertas de stock mínimo
- `/admin/pedidos` — cambiar estados de los pedidos
- `/admin/reportes` — KPIs, gráficos, kardex y exports a Excel/PDF

---

## 7. Si algo falla

**`Failed to determine a suitable driver class` o `Access denied for user`**
MySQL no está corriendo, o la contraseña no coincide. Revisá el paso 2.

**`Communications link failure`**
MySQL no está escuchando en el puerto 3306.

**El frontend carga pero todo dice "no se pudieron cargar los datos"**
El backend no está levantado, o está en otro puerto.

**Las peticiones fallan con error de CORS**
El backend solo acepta `http://localhost:5173`. Si Vite arrancó en otro puerto (pasa
cuando el 5173 está ocupado), cerrá lo que lo esté usando, o definí
`CORS_ORIGINS=http://localhost:PUERTO` antes de arrancar el backend.

**`port 8080 was already in use`**
Quedó un backend anterior corriendo. En Windows: `taskkill /F /IM java.exe`

---

## 8. Comandos útiles

Tests del backend:

```bash
./mvnw test
```

Chequeo de tipos del frontend (ESLint acá no es type-aware, esto sí):

```bash
npm run build
```

Linter del frontend:

```bash
npm run lint
```

---

## 9. Qué está implementado

Funcionando de punta a punta: **autenticación**, **catálogo** (productos, categorías,
galería de imágenes), **carrito**, **pedidos** (checkout, pago simulado, estados, kardex
de stock y comprobante PDF), **reportes** con exports y **alertas de inventario**.

Todavía no: **promociones/cupones** y **reseñas**. Las pantallas existen en el frontend y
degradan sin romperse — muestran un mensaje de "no se pudieron cargar" y el resto de la
app sigue andando.

Para el detalle de arquitectura y las convenciones del código, mirá [CLAUDE.md](CLAUDE.md).
