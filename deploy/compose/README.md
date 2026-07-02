# Docker Compose - IoT Smart Lab

Este deploy é a versão sem k3s/Kubernetes.

## Estrutura assumida

Executa os comandos a partir da raiz do repositório:

```text
IoT-smart-Lab/
├── DASHBOARD/iot-room/
├── SECURITY/iot-auth-api/
└── deploy/compose/
```

## 1. Preparar ambiente

```bash
cp deploy/compose/.env.example deploy/compose/.env
nano deploy/compose/.env
```

## 2. Escolher broker MQTT

### Modo A — Mosquitto dentro do Docker

Deixa assim:

```env
MQTT_BROKER=tcp://mosquitto:1883
```

Publicação de teste:

```bash
mosquitto_pub -h 127.0.0.1 -p 1883 -t esp32/temperatura -m "25.7"
mosquitto_pub -h 127.0.0.1 -p 1883 -t esp32/tds -m "210"
```

### Modo B — Mosquitto no Raspberry Pi

No `.env`, troca para o IP Tailscale do Pi:

```env
MQTT_BROKER=tcp://100.78.90.21:1883
```

## 3. Subir tudo

```bash
docker compose --env-file deploy/compose/.env -f deploy/compose/docker-compose.yml up -d --build
```

## 4. Ver logs

```bash
docker compose --env-file deploy/compose/.env -f deploy/compose/docker-compose.yml logs -f iot-room
```

## 5. Aceder

Dashboard direto:

```text
http://localhost:8081
```

Via Nginx:

```text
http://localhost:8088
```

Auth API:

```text
http://localhost:8090
```

## 6. Parar

```bash
docker compose --env-file deploy/compose/.env -f deploy/compose/docker-compose.yml down
```

## 7. Apagar volumes, incluindo BD

Atenção: isto apaga dados da MariaDB no Docker.

```bash
docker compose --env-file deploy/compose/.env -f deploy/compose/docker-compose.yml down -v
```

## Notas

- O ESP32 não entra em Docker.
- Tailscale deve ficar no host.
- Para ambiente real, podes continuar com systemd.
- Para ambiente replicável/teste, usa Docker Compose.
- Se já tens `init.sql`, coloca-o em `deploy/compose/db/init/`.
