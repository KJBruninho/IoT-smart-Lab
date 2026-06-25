# IoT Smart Lab

Sistema IoT para monitorização de uma sala/laboratório usando **ESP32**, **MQTT**, **Spring Boot**, **MySQL**, **dashboard web** e **app Android**.

> Estado: em desenvolvimento.

## Descrição

O **IoT Smart Lab** recolhe valores de sensores ligados a um ESP32 e envia-os por MQTT para uma aplicação Spring Boot. O backend recebe as mensagens, regista as leituras numa base de dados MySQL, mantém uma cache local em SQLite para tolerância a falhas e disponibiliza dados para visualização numa dashboard web e numa aplicação Android.

O fluxo principal cobre:

- leitura de temperatura;
- leitura de TDS;
- envio de dados por MQTT;
- armazenamento em MySQL;
- cache local SQLite;
- dashboard web com Thymeleaf;
- endpoints JSON para gráficos e estado do sistema;
- app Android para consulta dos dados;
- comandos remotos para controlo dos sensores.

O firmware do ESP32 também inclui suporte lógico para pH e calibração remota. A integração completa do pH no backend deve ser validada/adaptada conforme a configuração final.

## Arquitetura

```text
[Sensores]
  ├── Temperatura
  ├── TDS
  └── pH opcional
        |
        v
[ESP32]
        |
        | MQTT
        v
[Broker MQTT]
        |
        v
[Backend Spring Boot]
  ├── MySQL
  ├── SQLite local cache
  ├── Dashboard Web
  └── API para App Android
```

O Raspberry Pi 5 pode ser usado como servidor local para correr o broker MQTT, a base de dados e o backend.

## Funcionalidades

- Monitorização de temperatura.
- Monitorização de TDS.
- Suporte no firmware para sensor de pH.
- Publicação MQTT dos valores finais dos sensores.
- Modo de envio adaptativo: envio rápido quando há alterações e envio estável quando os valores estabilizam.
- Controlo remoto de sensores por comandos MQTT.
- Confirmação de comandos por ACK.
- Dashboard web por perfis de utilizador.
- Endpoints JSON para gráficos.
- Cache SQLite local para reduzir perda de dados em caso de falha na base de dados principal.
- App Android em Gradle/Kotlin.

## Estrutura do projeto

```text
IoT-smart-Lab/
├── APP/
│   ├── app/
│   ├── gradle/
│   ├── build.gradle.kts
│   └── settings.gradle.kts
│
├── DASHBOARD/
│   └── iot-room/
│       ├── src/main/java/com/iotroom/iotroom/
│       │   ├── config/
│       │   ├── controller/
│       │   ├── dto/
│       │   ├── model/
│       │   ├── mqtt/
│       │   ├── repository/
│       │   ├── security/
│       │   └── service/
│       ├── src/main/resources/
│       │   ├── static/
│       │   ├── templates/
│       │   └── application.properties
│       ├── pom.xml
│       └── README.md
│
├── ESP32/
│   └── sketch_esp32/
│       └── sketch_esp32.ino
│
├── SECURITY/
├── LICENSE
└── README.md
```

## Tecnologias

| Componente | Tecnologias |
|---|---|
| Firmware | ESP32, Arduino/C++, Wi-Fi, MQTT, PubSubClient, OneWire, DallasTemperature, Preferences |
| Backend/Dashboard | Java 17, Spring Boot, Spring Web, Thymeleaf, Spring Data JPA, Maven |
| Comunicação | MQTT |
| Base de dados | MySQL e SQLite local cache |
| App móvel | Android, Kotlin, Gradle |
| Servidor local | Raspberry Pi 5 ou outro computador na rede local |

## Tópicos MQTT

### Dados publicados pelo ESP32

| Tópico | Payload esperado | Descrição |
|---|---|---|
| `esp32/temperatura` | número decimal | Temperatura em ºC |
| `esp32/tds` | número decimal | Valor TDS em ppm |
| `esp32/ph` | número decimal | Valor de pH, opcional |

Nota: o backend atual está configurado para receber temperatura e TDS. Para usar pH no backend, acrescenta a configuração do tópico, o registo da leitura e a visualização correspondente.

### Controlo remoto do ESP32

O firmware usa tópicos por `DEVICE_ID`:

```text
esp32/{DEVICE_ID}/cmd
esp32/{DEVICE_ID}/ack
esp32/{DEVICE_ID}/status
```

Exemplo para `DEVICE_ID = esp32_sala_01`:

```text
esp32/esp32_sala_01/cmd
esp32/esp32_sala_01/ack
esp32/esp32_sala_01/status
```

Para o backend publicar comandos no mesmo formato, define o prefixo de comando como `esp32`:

```properties
mqtt.command-prefix=esp32
```

## Pré-requisitos

### Hardware

- ESP32.
- Sensor de temperatura compatível com OneWire/DallasTemperature.
- Sensor TDS analógico.
- Sensor pH analógico, opcional.
- Raspberry Pi 5 ou computador para correr backend, broker MQTT e base de dados.
- Fonte de alimentação.
- Cabos jumper.
- Breadboard ou PCB.
- Rede local Wi-Fi.

### Software

- Java 17.
- Maven.
- MySQL.
- Broker MQTT, por exemplo Mosquitto.
- Arduino IDE ou PlatformIO.
- Android Studio, para a app móvel.

## Instalação

### 1. Clonar o repositório

```bash
git clone https://github.com/KJBruninho/IoT-smart-Lab.git
cd IoT-smart-Lab
```

### 2. Configurar a base de dados

Cria a base de dados MySQL:

```sql
CREATE DATABASE iot_room CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

Se existir um ficheiro SQL de inicialização no projeto, importa-o para a base de dados:

```bash
mysql -u root -p iot_room < init_iot_room.sql
```

### 3. Configurar o backend Spring Boot

Abre o ficheiro:

```text
DASHBOARD/iot-room/src/main/resources/application.properties
```

Exemplo de configuração segura:

```properties
spring.application.name=iot-room
server.port=8081

spring.datasource.url=jdbc:mysql://localhost:3306/iot_room
spring.datasource.username=iot_user
spring.datasource.password=ALTERAR_PASSWORD
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
spring.jpa.hibernate.ddl-auto=none
spring.jpa.show-sql=false
spring.jpa.open-in-view=false

cache.sqlite.path=/var/lib/iot-room/cache.db
cache.flush.delay-ms=10000

mqtt.broker=tcp://localhost:1883
mqtt.client-id=iot-room-spring
mqtt.topic-temperatura=esp32/temperatura
mqtt.topic-tds=esp32/tds
mqtt.device-id=esp32_sala_01
mqtt.command-prefix=esp32

jwt.secret=ALTERAR_PARA_CHAVE_COM_32_CARACTERES_OU_MAIS
auth.api.base-url=http://localhost:8090
```

Não coloques passwords reais, chaves JWT ou IPs privados sensíveis em commits públicos.

### 4. Correr o backend/dashboard

```bash
cd DASHBOARD/iot-room
mvn spring-boot:run
```

Depois abre no browser:

```text
http://localhost:8081/
```

A aplicação redireciona o utilizador para a área adequada conforme o perfil configurado.

### 5. Configurar o ESP32

Abre o ficheiro:

```text
ESP32/sketch_esp32/sketch_esp32.ino
```

Atualiza os principais parâmetros:

```cpp
const char* DEVICE_ID = "esp32_sala_01";
const char* WIFI_SSID = "NOME_DA_REDE";
const char* WIFI_PASSWORD = "PALAVRA_PASSE";
const char* MQTT_SERVER = "IP_DO_BROKER_MQTT";
const int MQTT_PORT = 1883;
```

Confirma também os pinos usados:

```cpp
#define ONE_WIRE_BUS 18
#define TDS_PIN 34
#define PH_PIN 35
```

Depois carrega o firmware para o ESP32 usando Arduino IDE ou PlatformIO.

### 6. Correr a app Android

Abre a pasta `APP/` no Android Studio ou compila via Gradle:

```bash
cd APP
./gradlew build
```

Configura o endereço da API/backend na app de acordo com o IP do servidor local usado no teu ambiente.

## Endpoints principais

| Método | Endpoint | Descrição |
|---|---|---|
| `GET` | `/api/temperatura` | Devolve dados de temperatura para gráficos |
| `GET` | `/api/tds` | Devolve dados de TDS para gráficos |
| `GET` | `/api/dashboard/estado` | Devolve estado geral da dashboard, MQTT e últimas leituras |

## Comandos úteis do ESP32

No Serial Monitor, o firmware aceita comandos locais de teste:

```text
HELP
STATUS
PING
SET_REMOTE_ACTIVE:1
SET_REMOTE_ACTIVE:0
SET_SENSOR_ACTIVE:TEMPERATURA:1
SET_SENSOR_ACTIVE:TDS:0
SET_SENSOR_ACTIVE:PH:1
SET_CONNECTED:PH:0
RESET_CALIBRATION
```

Também é possível enviar comandos de configuração e calibração, por exemplo:

```text
SET_CALIBRATION:TDS:FACTOR=1.000000
SET_CALIBRATION:PH:FACTOR=1.000000;OFFSET=0.000000
SET_CONFIG:TDS:FAST=1000;STABLE=30000;FAST_DURATION=120000;DELTA=5.00
```

## Segurança

- Não guardar credenciais Wi-Fi, passwords da base de dados ou segredos JWT diretamente no repositório.
- Usar variáveis de ambiente ou ficheiros locais ignorados pelo Git para dados sensíveis.
- Alterar `jwt.secret` antes de correr em produção.
- Restringir o acesso ao broker MQTT na rede local.
- Criar utilizadores MySQL com permissões limitadas.
- Usar firewall no Raspberry Pi ou servidor local.
- Ativar HTTPS quando a dashboard estiver exposta fora da rede local.

## Roadmap

- Integrar completamente o sensor pH no backend e na dashboard.
- Adicionar documentação de ligações elétricas dos sensores.
- Criar `docker-compose.yml` para MySQL, MQTT e backend.
- Adicionar testes automáticos ao backend.
- Melhorar documentação da app Android.
- Adicionar exportação CSV.
- Adicionar alertas configuráveis por sensor.

## Autor

Bruno Marinho

## Licença

Este projeto está licenciado sob a licença MIT.
