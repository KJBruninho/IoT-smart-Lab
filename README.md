# Sala IoT — Monitorização com ESP32 e Raspberry Pi 5

Projeto IoT para monitorização de sensores de **temperatura** e **TDS** numa sala, utilizando **ESP32** e **Raspberry Pi 5**.

Os dados recolhidos são enviados para uma base de dados e posteriormente apresentados numa **dashboard web** e numa **aplicação móvel**.

---

## Objetivo

Este projeto tem como objetivo criar um sistema de monitorização IoT capaz de:

* Ler dados de sensores de temperatura
* Ler dados de sensores TDS
* Enviar os dados para uma base de dados
* Armazenar o histórico das medições
* Disponibilizar os dados para visualização numa dashboard
* Servir os dados para uma app móvel

---

## Arquitetura do Sistema

```text
[Sensores]
   |
   | Temperatura / TDS
   |
[ESP32]
   |
   | Wi-Fi / MQTT / HTTP
   |
[Raspberry Pi 5]
   |
   | API / Broker / Backend
   |
[Base de Dados]
   |
   | Dados históricos
   |
[Dashboard Web] + [App Mobile]
```

---

## Hardware Utilizado

* ESP32
* Raspberry Pi 5
* Sensor de temperatura
* Sensor TDS
* Fonte de alimentação
* Cabos jumper
* Breadboard ou PCB
* Rede Wi-Fi local

---

## Tecnologias Previstas

### Microcontrolador

* ESP32
* Arduino IDE ou PlatformIO
* Wi-Fi
* MQTT ou HTTP

### Servidor Local

* Raspberry Pi 5
* Python / Node.js
* API REST
* MQTT Broker, por exemplo Mosquitto
* Docker, opcional

### Base de Dados

* PostgreSQL
* MySQL
* InfluxDB
* SQLite, para testes locais

### Visualização

* Dashboard web
* App móvel
* Gráficos em tempo real
* Histórico de medições

---

## Estrutura do Projeto

```text
sala-iot/
│
├── esp32/
│   ├── src/
│   ├── include/
│   └── README.md
│
├── raspberry-pi/
│   ├── backend/
│   ├── database/
│   └── README.md
│
├── dashboard/
│   ├── src/
│   └── README.md
│
├── app/
│   ├── src/
│   └── README.md
│
├── docs/
│   ├── arquitetura.md
│   └── esquemas.md
│
└── README.md
```

---

## Funcionamento

1. O ESP32 lê os valores dos sensores de temperatura e TDS.
2. Os dados são tratados e formatados.
3. O ESP32 envia os dados para o Raspberry Pi 5 através da rede.
4. O Raspberry Pi recebe os dados através de uma API ou broker MQTT.
5. Os dados são guardados numa base de dados.
6. A dashboard e a app consultam a base de dados através do backend.
7. O utilizador consegue visualizar os valores atuais e o histórico.

---

## Exemplo de Dados Enviados

```json
{
  "device_id": "esp32_sala_01",
  "temperature": 24.6,
  "tds": 438,
  "timestamp": "2026-05-18T14:30:00Z"
}
```

---

## Possíveis Endpoints da API

| Método | Endpoint               | Descrição                     |
| ------ | ---------------------- | ----------------------------- |
| `POST` | `/api/readings`        | Recebe dados dos sensores     |
| `GET`  | `/api/readings`        | Lista medições                |
| `GET`  | `/api/readings/latest` | Obtém a última medição        |
| `GET`  | `/api/devices`         | Lista dispositivos registados |

---

## Exemplo de Tabela na Base de Dados

```sql
CREATE TABLE sensor_readings (
    id SERIAL PRIMARY KEY,
    device_id VARCHAR(100) NOT NULL,
    temperature FLOAT,
    tds FLOAT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

---

## Configuração do ESP32

O ESP32 deverá ser configurado com:

* SSID da rede Wi-Fi
* Palavra-passe da rede
* Endereço IP ou domínio do Raspberry Pi
* Intervalo de leitura dos sensores
* Identificador do dispositivo

Exemplo de variáveis:

```cpp
const char* WIFI_SSID = "NOME_DA_REDE";
const char* WIFI_PASSWORD = "PALAVRA_PASSE";
const char* SERVER_URL = "http://192.168.1.100:3000/api/readings";
const char* DEVICE_ID = "esp32_sala_01";
```

---

## Configuração do Raspberry Pi 5

No Raspberry Pi 5 será executado o backend responsável por:

* Receber os dados enviados pelo ESP32
* Validar os dados recebidos
* Guardar os dados na base de dados
* Disponibilizar endpoints para dashboard e app
* Opcionalmente gerir autenticação e permissões

---

## Instalação

### 1. Clonar o repositório

```bash
git clone https://github.com/teu-utilizador/sala-iot.git
cd sala-iot
```

### 2. Configurar o backend

```bash
cd raspberry-pi/backend
```

Instalar dependências:

```bash
npm install
```

ou, caso seja usado Python:

```bash
pip install -r requirements.txt
```

### 3. Configurar variáveis de ambiente

Criar um ficheiro `.env`:

```env
PORT=3000
DATABASE_URL=postgresql://user:password@localhost:5432/sala_iot
MQTT_HOST=localhost
MQTT_PORT=1883
```

### 4. Iniciar o backend

```bash
npm run dev
```

ou:

```bash
python app.py
```

### 5. Carregar código para o ESP32

Abrir o projeto na Arduino IDE ou PlatformIO, configurar as credenciais Wi-Fi e carregar o firmware para o ESP32.

---

## Funcionalidades Planeadas

* [ ] Leitura de temperatura
* [ ] Leitura de TDS
* [ ] Envio de dados por HTTP
* [ ] Envio de dados por MQTT
* [ ] Armazenamento em base de dados
* [ ] Dashboard web
* [ ] App móvel
* [ ] Alertas de valores fora do intervalo
* [ ] Autenticação de utilizadores
* [ ] Histórico por dia, semana e mês
* [ ] Exportação de dados em CSV

---

## Segurança

Algumas medidas recomendadas:

* Não guardar credenciais diretamente no código
* Utilizar ficheiro `.env` no backend
* Proteger endpoints da API
* Usar autenticação na dashboard e app
* Validar todos os dados recebidos
* Configurar firewall no Raspberry Pi
* Usar HTTPS em ambiente de produção

---

## Estado do Projeto

Em desenvolvimento.

---

## Autor

**Bruno Marinho**

---

## Licença

Este projeto pode ser distribuído sob a licença MIT.
