#!/usr/bin/env bash
set -euo pipefail

FILE="${1:-ESP32/sketch_esp32/sketch_esp32.ino}"

if [ ! -f "$FILE" ]; then
  echo "Ficheiro não encontrado: $FILE" >&2
  exit 1
fi

sed -i 's/DELTA=0\.20/DELTA=0.10/g' "$FILE"
sed -i 's/float tempDeltaLimit = 0\.20;/float tempDeltaLimit = 0.10;/g' "$FILE"

echo "Temperatura delta atualizada para 0.10 em $FILE"
grep -n "tempDeltaLimit\|SET_CONFIG:TEMPERATURA" "$FILE" || true
