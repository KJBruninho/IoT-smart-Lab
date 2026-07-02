#!/usr/bin/env bash
set -euo pipefail

SKETCH_DIR="${SKETCH_DIR:-/workspace/sketch_esp32}"
FQBN="${FQBN:-esp32:esp32:esp32}"

echo "Compiling ESP32 sketch:"
echo "  sketch: ${SKETCH_DIR}"
echo "  fqbn:   ${FQBN}"

arduino-cli compile --fqbn "${FQBN}" "${SKETCH_DIR}"
