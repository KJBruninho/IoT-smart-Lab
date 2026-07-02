#!/usr/bin/env bash
set -euo pipefail

MQTT_HOST="${MQTT_HOST:-100.78.90.21}"
MQTT_PORT="${MQTT_PORT:-1883}"
DEVICE_ID="${DEVICE_ID:-esp32_sala_01}"

TOPIC="esp32/${DEVICE_ID}/cmd"
PAYLOAD="{\"commandId\":9001,\"deviceId\":\"${DEVICE_ID}\",\"tipoSensor\":\"TEMPERATURA\",\"comando\":\"SET_CONFIG:TEMPERATURA:FAST=1000;STABLE=30000;FAST_DURATION=120000;DELTA=0.10\"}"

echo "A enviar configuração para ${MQTT_HOST}:${MQTT_PORT}"
echo "Topic: ${TOPIC}"
echo "Payload: ${PAYLOAD}"

mosquitto_pub -h "$MQTT_HOST" -p "$MQTT_PORT" -t "$TOPIC" -m "$PAYLOAD"
