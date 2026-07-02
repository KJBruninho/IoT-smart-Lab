# Docker Compose completo - IoT Smart Lab

Este pacote cobre a parte dockerizável do repositório:

- `DASHBOARD/iot-room` → backend/dashboard Spring Boot.
- `SECURITY` → API de autenticação Spring Boot.
- `CHAT-BOT` → API do assistente Spring Boot.
- `MariaDB`.
- `Mosquitto`.
- `Nginx`.
- `APP` → build-only Dockerfile para Android/Gradle.
- `ESP32` → build-only Dockerfile para Arduino CLI.

O ESP32 e a app Android não são serviços de servidor. Por isso entram como build-tools, não como containers permanentes.

## 1. Instalar ficheiros

A partir da raiz do repositório:

```bash
unzip iot-smart-lab-docker-full.zip -d .
chmod +x deploy/scripts/*.sh
chmod +x ESP32/build-esp32.sh
```

## 2. Configurar `.env`

```bash
cp deploy/compose/.env.example deploy/compose/.env
nano deploy/compose/.env
```

### MQTT dentro do Docker

```env
MQTT_BROKER=tcp://mosquitto:1883
```

### MQTT no Raspberry Pi

```env
MQTT_BROKER=tcp://100.78.90.21:1883
```

## 3. Subir a stack

```bash
docker compose --env-file deploy/compose/.env -f deploy/compose/docker-compose.yml up -d --build
```

## 4. Ver logs

```bash
docker compose --env-file deploy/compose/.env -f deploy/compose/docker-compose.yml logs -f iot-room
```

## 5. URLs

- Dashboard direto: `http://localhost:8081`
- Nginx: `http://localhost:8088`
- Auth API: `http://localhost:8090`
- Assistant API: `http://localhost:8092`
- MQTT: `localhost:1883`

## 6. Teste MQTT

```bash
mosquitto_pub -h 127.0.0.1 -p 1883 -t esp32/temperatura -m "25.7"
mosquitto_pub -h 127.0.0.1 -p 1883 -t esp32/tds -m "210"
```

## 7. Temperatura delta 0.10

Este pacote inclui duas formas.

### Alterar o firmware por ficheiro

```bash
deploy/scripts/apply-temp-delta-0.10.sh
```

Isto troca:

```cpp
float tempDeltaLimit = 0.20;
```

por:

```cpp
float tempDeltaLimit = 0.10;
```

### Enviar comando remoto ao ESP32

Com o ESP32 online:

```bash
MQTT_HOST=100.78.90.21 DEVICE_ID=esp32_sala_01 deploy/scripts/send-temp-delta-0.10.sh
```

Ou por Compose:

```bash
docker compose --env-file deploy/compose/.env -f deploy/compose/docker-compose.yml --profile device-config up esp32-temp-delta-01
```

## 8. Build da app Android

```bash
docker compose --env-file deploy/compose/.env -f deploy/compose/docker-compose.yml --profile build-tools run --rm android-build
```

Nota: se o projeto Android exigir Android SDK completo, usa este Dockerfile como base e troca para uma imagem com SDK Android.

## 9. Build do firmware ESP32

```bash
docker compose --env-file deploy/compose/.env -f deploy/compose/docker-compose.yml --profile build-tools run --rm esp32-firmware-build
```

Se a tua placa exigir outro FQBN:

```bash
FQBN=esp32:esp32:esp32s3 docker compose --env-file deploy/compose/.env -f deploy/compose/docker-compose.yml --profile build-tools run --rm esp32-firmware-build
```

## 10. Parar

```bash
docker compose --env-file deploy/compose/.env -f deploy/compose/docker-compose.yml down
```

## 11. Apagar volumes

Atenção: apaga a BD Docker.

```bash
docker compose --env-file deploy/compose/.env -f deploy/compose/docker-compose.yml down -v
```

## Notas

- Não guardes passwords reais no `.env.example`.
- Para produção real, usa passwords próprias e MQTT com autenticação.
- Tailscale deve ficar no host.
- Este pacote não usa k3s/Kubernetes.
