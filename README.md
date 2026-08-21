# Krypton

E-commerce del Proyecto Integrador — CIBERTEC VI.

- **Backend**: Spring Boot 4.1 · Java 21 · MySQL · JWT
- **Frontend**: React 19 · TypeScript · Vite

Hay dos formas de levantarlo. **Si es tu primera vez, usá Docker**: es un comando y no
tenés que instalar nada más.

| | [Con Docker](#opción-a--con-docker-recomendado) | [A mano](#opción-b--sin-docker) |
|---|---|---|
| Hay que instalar | Docker Desktop | JDK 21 · Node 20+ · MySQL 8 |
| Comandos para arrancar | 1 | 3 |
| La base viene con datos | Sí, sola | Hay que importarla |

---

## Opción A — Con Docker (recomendado)

Necesitás **Docker Desktop** instalado y abierto (que diga *Engine running*).

```bash
docker compose up --build
```

Y listo. Eso levanta MySQL, el backend y el frontend, con la base **ya cargada con el
catálogo de demo y las fotos**.

- Frontend → **http://localhost:5173**
- Backend → http://localhost:8080
- MySQL → `localhost:3307`

La primera vez tarda unos minutos porque compila las dos aplicaciones. Las siguientes
son segundos.

### Comandos del día a día

| Para | Comando |
|---|---|
| Frenar todo | `docker compose down` |
| Frenar y **borrar la base** (recarga el seed al volver a subir) | `docker compose down -v` |
| Ver los logs del backend | `docker compose logs -f backend` |
| Reconstruir después de cambiar código | `docker compose up --build` |

> **¿Por qué MySQL en el 3307 y no el 3306?** Para no chocar con el MySQL que quizás ya
> tenés instalado en tu máquina. Dentro de Docker los contenedores se hablan por el 3306
> igual; el 3307 es solo la puerta desde afuera, por si querés conectarte con Workbench.

---

## Opción B — Sin Docker

### 1. Qué necesitás instalado

| | Versión | Cómo verificar |
|---|---|---|
| **JDK** | 21 | `java -version` |
| **Node.js** | 20 o superior | `node --version` |
| **MySQL** | 8.x, corriendo en `localhost:3306` | que el servicio esté iniciado |

Maven **no** hace falta instalarlo: el repo trae el wrapper (`mvnw`).

### 2. Configurar la base de datos

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

Esas variables valen solo para esa terminal. Si abrís una nueva, definilas otra vez.

Después, traé el catálogo de demo que viene en el repo:

```bash
bash db/import.sh
```

> ⚠ Reemplaza tu base `krypton` local.

### 3. Levantar el backend

Desde la **raíz** del proyecto:

```bash
./mvnw spring-boot:run
```

En PowerShell, si `./mvnw` no te funciona, usá `.\mvnw.cmd spring-boot:run`.

Queda en **http://localhost:8080**. Arrancó bien cuando ves:

```
Started ProyectoIntegradorApplication in 8.0 seconds
```

### 4. Levantar el frontend

En **otra terminal**, desde la carpeta `frontend/`:

```bash
npm install
```

```bash
npm run dev
```

Queda en **http://localhost:5173**. El `npm install` solo hace falta la primera vez.

El frontend apunta a `http://localhost:8080` por defecto. Para cambiarlo, creá
`frontend/.env.local` con `VITE_API_BASE_URL=http://otro-host:puerto`.

### 5. Orden de arranque

1. **MySQL** — si no está corriendo, el backend no levanta.
2. **Backend**
3. **Frontend**

El frontend levanta igual sin el backend, pero cada pantalla va a mostrar errores de
carga: no tiene de dónde traer los datos.

---

## Entrar como administrador

La base de demo ya trae un admin:

```
admin@krypton.pe
Krypton.Admin.2026
```

Si arrancás con una base vacía, el backend crea uno solo y lo avisa en la consola con un
recuadro grande. Es idempotente: si ya existe un admin, no hace nada.

---

## Probar que funciona

Entrá a http://localhost:5173 y recorré:

1. **Catálogo** → abrí un producto → **Agregar al carrito**
2. **Carrito** → **Finalizar compra** (te pide iniciar sesión; creá una cuenta)
3. Completá boleta con un DNI de 8 dígitos → **Confirmar pedido**
4. En el detalle del pedido → **Pagar** (es simulado, no cobra nada)
5. **Descargar boleta** → te baja el PDF

Y con el admin:

- `/admin/productos` — alta, edición, imágenes, alertas de stock mínimo y reactivar eliminados
- `/admin/pedidos` — cambiar estados de los pedidos
- `/admin/reportes` — KPIs, gráficos, kardex y exports a Excel/PDF

---

## Compartir los datos que cargues

El catálogo viaja por el repo: el volcado en `db/krypton_seed.sql` y las fotos en
`uploads/`. Si cargás productos nuevos desde el panel y querés que le lleguen al resto:

Con Docker (la base está en el contenedor, puerto 3307):

```bash
DB_PORT=3307 bash db/export.sh
```

Sin Docker:

```bash
bash db/export.sh
```

Y después commiteá **las dos cosas** — sin los archivos, tus compañeros ven los productos
sin foto:

```bash
git add db/krypton_seed.sql uploads/ && git commit -m "data: actualiza catalogo de demo"
```

> El volcado es **un archivo**: si dos personas exportan a la vez, el segundo `push` pisa
> al primero. Mientras estén cargando datos, pónganse de acuerdo en quién exporta.

Más detalle en [`db/README.md`](db/README.md).

---

## Si algo falla

### Con Docker

**`failed to connect to the docker API` / `the daemon is not running`**
Docker Desktop no está abierto. Abrilo y esperá a que diga *Engine running*.

**`port is already allocated`**
Tenés algo ocupando el 8080, el 5173 o el 3307. Frená tu backend o tu Vite local
(`taskkill /F /IM java.exe`, `taskkill /F /IM node.exe`).

**Cambié el seed y sigue apareciendo el catálogo viejo**
El `.sql` se ejecuta solo cuando se crea la base. Borrá el volumen: `docker compose down -v`

**Cambié código y no se ve el cambio**
Falta reconstruir la imagen: `docker compose up --build`

### Sin Docker

**`Failed to determine a suitable driver class` o `Access denied for user`**
MySQL no está corriendo, o la contraseña no coincide.

**`Communications link failure`**
MySQL no está escuchando en el 3306.

**El frontend carga pero todo dice "no se pudieron cargar los datos"**
El backend no está levantado, o está en otro puerto.

**Las peticiones fallan con error de CORS**
El backend solo acepta `http://localhost:5173`. Si Vite arrancó en otro puerto (pasa
cuando el 5173 está ocupado), cerrá lo que lo esté usando, o definí
`CORS_ORIGINS=http://localhost:PUERTO` antes de arrancar el backend.

**`port 8080 was already in use`**
Quedó un backend anterior corriendo: `taskkill /F /IM java.exe`

---

## Comandos útiles

| Para | Comando |
|---|---|
| Tests del backend | `./mvnw test` |
| Chequeo de tipos del frontend | `npm run build` |
| Linter del frontend | `npm run lint` |

`npm run build` es la única forma de tener chequeo de tipos real: el ESLint de este
proyecto no es type-aware.

---

## Qué está implementado

Funcionando de punta a punta: **autenticación**, **catálogo** (productos, categorías,
galería de imágenes), **carrito**, **pedidos** (checkout, pago simulado, estados, kardex
de stock y comprobante PDF), **reportes** con exports a Excel/PDF y **alertas de
inventario**.

Todavía no: **promociones/cupones** y **reseñas**. Las pantallas existen en el frontend y
degradan sin romperse — muestran un mensaje de "no se pudieron cargar" y el resto de la
app sigue andando.

Para el detalle de arquitectura y las convenciones del código, mirá [docs/ARQUITECTURA.md](docs/ARQUITECTURA.md).
