# IoT Room

Aplicação única Spring Boot para:
- receber dados MQTT do ESP32;
- gravar leituras em MySQL;
- mostrar dashboard web com Thymeleaf;
- fornecer endpoints JSON para gráficos.

## Antes de correr

1. Importar o ficheiro `init_iot_room.sql` na base de dados MySQL.
2. Editar `src/main/resources/application.properties`:

```properties
spring.datasource.username=root
spring.datasource.password=A_TUA_PASSWORD
mqtt.broker=tcp://localhost:1883
mqtt.device-id=esp32_sala_01
```

## Correr

```bash
mvn spring-boot:run
```

Abrir:

```text
http://localhost:8080/dashboard
```

## Tópicos MQTT usados

```text
esp32/temperatura
esp32/tds
```

Payload esperado:

```text
30.06
896
```
