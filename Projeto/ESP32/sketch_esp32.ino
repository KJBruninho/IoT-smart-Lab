#include <WiFi.h>
#include <PubSubClient.h>
#include <OneWire.h>
#include <DallasTemperature.h>

// =====================================
// WIFI
// =====================================
const char* ssid = "PiHotspot";
const char* password = "pi123456789";

// =====================================
// MQTT
// =====================================
const char* mqtt_server = "192.168.4.1";
const int mqtt_port = 1883;

// =====================================
// TOPICOS MQTT
// =====================================
const char* topicTemp = "esp32/temperatura";
const char* topicCmd  = "esp32/comando";

// =====================================
// PINOS e ONEWIRE + DS18B20
// =====================================
#define ONE_WIRE_BUS 18

OneWire oneWire(ONE_WIRE_BUS);
DallasTemperature sensors(&oneWire);

// =====================================
// WIFI + MQTT
// =====================================
WiFiClient espClient;
PubSubClient client(espClient);

// =====================================
// TIMER
// =====================================
unsigned long lastTempUpdate = 0;
const long interval = 5000;

// =====================================
// WIFI
// =====================================
void setup_wifi() {

  delay(10);

  Serial.println();
  Serial.print("A Ligar ao AP do Pi: ");
  Serial.println(ssid);

  WiFi.mode(WIFI_STA);
  WiFi.begin(ssid, password);

  while (WiFi.status() != WL_CONNECTED) {

    delay(500);
    Serial.print(".");
  }

  WiFi.setSleep(false);

  Serial.println("");
  Serial.println("Conectado!");
  Serial.print("IP: ");
  Serial.println(WiFi.localIP());
}

// =====================================
// MQTT RECONNECT
// =====================================
void reconnect() {

  while (!client.connected()) {

    Serial.print("A ligar por MQTT...");

    String clientId = "ESP32-";
    clientId += String(random(0xffff), HEX);

    if (client.connect(clientId.c_str())) {

      Serial.println(" conectado!");

      client.subscribe(topicCmd);

    } else {

      Serial.print(" falhou, rc=");
      Serial.print(client.state());
      Serial.println(" tentando novamente em 5 segundos");

      delay(1000);
    }
  }
}

// =====================================
// SETUP
// =====================================
void setup() {

  Serial.begin(115200);

  Serial.println("A iniciar o sensor de temperatura...");

  sensors.begin();

  setup_wifi();

  client.setServer(mqtt_server, mqtt_port);

  Serial.println("Sistema iniciado.");
}

// =====================================
// LOOP
// =====================================
void loop() {

  if (!client.connected()) {
    reconnect();
  }

  client.loop();

  unsigned long currentMillis = millis();

  if (currentMillis - lastTempUpdate >= interval) {

    lastTempUpdate = currentMillis;

    sensors.requestTemperatures();

    delay(100);

    float tempC = sensors.getTempCByIndex(0);

    if (tempC != DEVICE_DISCONNECTED_C) {

      Serial.print("Temperatura: ");
      Serial.print(tempC);
      Serial.println(" C");

      char tempString[10];

      dtostrf(tempC, 1, 2, tempString);

      client.publish(topicTemp, tempString);

    } else {

      Serial.println("Erro: Sensor de temperatura não encontrado!");
    }
  }
}