# Docker no IoT Smart Lab

Este diretório contém um deploy sem Kubernetes.

## Serviços permanentes

- MariaDB
- Mosquitto
- iot-auth
- iot-assistant
- iot-room
- nginx

## Build tools

- Android app
- ESP32 firmware

## Limite de temperatura

Foi incluído patch/script para baixar o delta de temperatura de `0.20` para `0.10`.

```bash
deploy/scripts/apply-temp-delta-0.10.sh
```
