# Base de datos compartida

Acá viaja el catálogo de demo (categorías, productos, fotos, usuarios y pedidos)
para que todo el equipo trabaje sobre los mismos datos.

## Cómo funciona

- **`krypton_seed.sql`** — volcado completo de la base `krypton`.
- **`../uploads/`** — los archivos de las fotos de producto. Van al repo a propósito:
  la tabla `product_image` guarda solo el nombre del archivo, así que si viajan las
  filas y no los archivos, cada imagen responde 404.

## Si querés TRAER los datos del repo

```bash
bash db/import.sh
```

> ⚠ **Reemplaza tu base local.** Lo que tengas cargado en `krypton` se pierde.
> Si tenías algo que querés conservar, exportá primero.

Las fotos no hay que hacer nada: ya llegaron con el `git pull`.

## Si querés COMPARTIR lo que cargaste

Después de cargar productos, categorías o fotos desde el panel de admin:

```bash
bash db/export.sh
```

```bash
git add db/krypton_seed.sql uploads/ && git commit -m "data: actualiza catalogo de demo"
```

No te olvides de `uploads/`: sin los archivos, tus compañeros ven los productos sin foto.

## Credenciales y rutas distintas

Los scripts asumen MySQL 8.4 en la ruta por defecto de Windows y usuario `root`.
Si en tu máquina es distinto:

```bash
MYSQL_BIN="/c/ruta/a/mysql.exe" DB_USER=root DB_PASSWORD=tu_password bash db/import.sh
```

## Por qué un volcado y no un `data.sql` automático

Spring Boot puede correr un `data.sql` solo en cada arranque, pero lo hace **siempre**:
en el segundo arranque los INSERT chocan contra las filas que ya existen, y peor, te
sobrescribiría en silencio lo que hayas cargado a mano mientras trabajás.

Un import explícito, que corrés cuando vos querés, no te sorprende nunca. La
sincronización es manual justamente para que sea predecible.

## Ojo con esto

El volcado es una **foto de un momento**. Si vos y un compañero cargan productos
distintos y los dos exportan, el segundo `git push` pisa al primero — es un archivo,
git no sabe mezclar filas de SQL.

Mientras estén cargando datos, pónganse de acuerdo en quién exporta.
