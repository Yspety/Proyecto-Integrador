#!/usr/bin/env bash
# Exporta la base krypton a db/krypton_seed.sql para compartirla por el repo.
#
# Uso (desde la raíz del proyecto, en Git Bash):
#   bash db/export.sh
#
# Si tu MySQL no está en la ruta por defecto de Windows, indicá dónde:
#   MYSQL_BIN="/c/ruta/a/mysqldump.exe" bash db/export.sh
#
# Y si tu usuario/contraseña no son root/mysql:
#   DB_USER=root DB_PASSWORD=tu_password bash db/export.sh
set -euo pipefail

MYSQL_BIN="${MYSQL_BIN:-/c/Program Files/MySQL/MySQL Server 8.4/bin/mysqldump.exe}"
DB_USER="${DB_USER:-root}"
DB_PASSWORD="${DB_PASSWORD:-mysql}"
OUT="db/krypton_seed.sql"

if [ ! -x "$MYSQL_BIN" ] && ! command -v "$MYSQL_BIN" > /dev/null 2>&1; then
  echo "No encuentro mysqldump en: $MYSQL_BIN"
  echo "Pasá la ruta con MYSQL_BIN=... (ver el encabezado de este script)."
  exit 1
fi

# --databases + --add-drop-database: el archivo recrea el schema completo, así el
# que lo importa queda con exactamente lo mismo que vos, sin restos previos.
# --complete-insert: los INSERT nombran las columnas, así que el dump no se rompe
# si mañana Hibernate agrega una columna nueva.
"$MYSQL_BIN" \
  -u "$DB_USER" -p"$DB_PASSWORD" \
  --databases krypton \
  --add-drop-database \
  --complete-insert \
  --default-character-set=utf8mb4 \
  --skip-dump-date \
  > "$OUT"

echo "Exportado a $OUT ($(wc -c < "$OUT") bytes)"
echo
echo "Acordate de commitear TAMBIÉN la carpeta uploads/ si subiste fotos nuevas:"
echo "  git add $OUT uploads/ && git commit -m \"data: actualiza catalogo de demo\""
