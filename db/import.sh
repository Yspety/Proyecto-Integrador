#!/usr/bin/env bash
# Importa db/krypton_seed.sql a tu MySQL local.
#
# ⚠  BORRA Y RECREA la base `krypton`. Todo lo que tengas cargado localmente se
#    pierde y queda reemplazado por lo que trae el repo. Si cargaste cosas que
#    querés conservar, corré antes `bash db/export.sh` y guardate ese archivo.
#
# Uso (desde la raíz del proyecto, en Git Bash):
#   bash db/import.sh
#
# Rutas y credenciales, igual que en export.sh:
#   MYSQL_BIN="/c/ruta/a/mysql.exe" DB_USER=root DB_PASSWORD=tu_password bash db/import.sh
set -euo pipefail

MYSQL_BIN="${MYSQL_BIN:-/c/Program Files/MySQL/MySQL Server 8.4/bin/mysql.exe}"
DB_USER="${DB_USER:-root}"
DB_PASSWORD="${DB_PASSWORD:-mysql}"
IN="db/krypton_seed.sql"

if [ ! -f "$IN" ]; then
  echo "No existe $IN. ¿Estás parado en la raíz del proyecto?"
  exit 1
fi

if [ ! -x "$MYSQL_BIN" ] && ! command -v "$MYSQL_BIN" > /dev/null 2>&1; then
  echo "No encuentro mysql en: $MYSQL_BIN"
  echo "Pasá la ruta con MYSQL_BIN=... (ver el encabezado de este script)."
  exit 1
fi

echo "Esto REEMPLAZA tu base 'krypton' local por la del repo."
read -r -p "¿Seguro? [s/N] " ok
case "$ok" in
  s|S|si|SI|y|Y) ;;
  *) echo "Cancelado."; exit 0 ;;
esac

"$MYSQL_BIN" -u "$DB_USER" -p"$DB_PASSWORD" --default-character-set=utf8mb4 < "$IN"

echo "Listo. Las fotos ya están en uploads/ (vienen con el repo)."
echo "Levantá el backend y entrá con las credenciales del README."
