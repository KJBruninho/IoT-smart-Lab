#include <WiFi.h>
#include <PubSubClient.h>
#include <OneWire.h>
#include <DallasTemperature.h>
#include <math.h>
#include <Preferences.h>

// =====================================================
// CONFIGURAÇÃO WIFI
// =====================================================
const char* WIFI_SSID     = "PiHotspot";
const char* WIFI_PASSWORD = "pi123456789";

// =====================================================
// CONFIGURAÇÃO MQTT
// =====================================================
const char* MQTT_SERVER = "192.168.4.1";
const int   MQTT_PORT   = 1883;

const char* TOPIC_TEMP = "esp32/temperatura";
const char* TOPIC_TDS  = "esp32/tds";

// Opcional: publicar também o fator de calibração
const char* TOPIC_TDS_FACTOR = "esp32/tds/fator";

// =====================================================
// PINOS
// =====================================================
#define ONE_WIRE_BUS 18
#define TDS_PIN      34

// =====================================================
// CONFIGURAÇÃO TDS
// =====================================================
#define VREF        3.3
#define ADC_MAX     4095.0
#define SCOUNT      30

int analogBuffer[SCOUNT];
int analogBufferIndex = 0;
bool bufferReady = false;

float tdsFactor = 1.0;

// =====================================================
// OBJETOS
// =====================================================
OneWire oneWire(ONE_WIRE_BUS);
DallasTemperature sensors(&oneWire);

WiFiClient espClient;
PubSubClient mqttClient(espClient);

Preferences preferences;

// =====================================================
// TIMERS
// =====================================================
unsigned long lastMainUpdate = 0;
unsigned long lastAnalogRead = 0;

const unsigned long MAIN_INTERVAL_MS   = 1000;
const unsigned long ANALOG_INTERVAL_MS = 40;

// =====================================================
// WIFI
// =====================================================
void setupWiFi() {
  Serial.print("A ligar ao WiFi");

  WiFi.mode(WIFI_STA);
  WiFi.begin(WIFI_SSID, WIFI_PASSWORD);

  while (WiFi.status() != WL_CONNECTED) {
    delay(500);
    Serial.print(".");
  }

  WiFi.setSleep(false);

  Serial.println();
  Serial.println("WiFi conectado.");
  Serial.print("IP: ");
  Serial.println(WiFi.localIP());
}

// =====================================================
// MQTT
// =====================================================
void reconnectMQTT() {
  while (!mqttClient.connected()) {
    Serial.print("A ligar ao MQTT... ");

    String clientId = "ESP32-";
    clientId += String(random(0xffff), HEX);

    if (mqttClient.connect(clientId.c_str())) {
      Serial.println("conectado.");
    } else {
      Serial.print("falhou. Estado: ");
      Serial.println(mqttClient.state());
      delay(2000);
    }
  }
}

// =====================================================
// LEITURA DE TEMPERATURA
// =====================================================
float readTemperature() {
  sensors.requestTemperatures();
  float tempC = sensors.getTempCByIndex(0);

  // DS18B20 retorna -127 quando há erro de leitura
  if (tempC == DEVICE_DISCONNECTED_C || tempC < -50 || tempC > 125) {
    return NAN;
  }

  return tempC;
}

// =====================================================
// FILTRO DE MEDIANA
// =====================================================
int getMedianValue(int buffer[], int size) {
  int sorted[size];

  for (int i = 0; i < size; i++) {
    sorted[i] = buffer[i];
  }

  for (int i = 0; i < size - 1; i++) {
    for (int j = i + 1; j < size; j++) {
      if (sorted[i] > sorted[j]) {
        int temp = sorted[i];
        sorted[i] = sorted[j];
        sorted[j] = temp;
      }
    }
  }

  if (size % 2 == 1) {
    return sorted[size / 2];
  } else {
    return (sorted[size / 2] + sorted[(size / 2) - 1]) / 2;
  }
}

// =====================================================
// AMOSTRAGEM ANALÓGICA TDS
// =====================================================
void sampleTDS() {
  if (millis() - lastAnalogRead >= ANALOG_INTERVAL_MS) {
    lastAnalogRead = millis();

    analogBuffer[analogBufferIndex] = analogRead(TDS_PIN);
    analogBufferIndex++;

    if (analogBufferIndex >= SCOUNT) {
      analogBufferIndex = 0;
      bufferReady = true;
    }
  }
}

// =====================================================
// CÁLCULO TDS SEM FATOR
// Usado internamente para calibração
// =====================================================
float calculateRawTDS(float temperature) {
  if (!bufferReady) return NAN;
  if (isnan(temperature)) return NAN;

  int medianADC = getMedianValue(analogBuffer, SCOUNT);

  // Conversão ADC -> tensão
  float voltage = medianADC * (VREF / ADC_MAX);

  // Compensação de temperatura
  // Fórmula padrão: 2% por grau em relação a 25 ºC
  float compensationCoefficient = 1.0 + 0.02 * (temperature - 25.0);

  if (compensationCoefficient <= 0) return NAN;

  float compensationVoltage = voltage / compensationCoefficient;

  // Fórmula típica do sensor TDS analógico
  // Resultado em ppm
  float tds =
    (133.42 * pow(compensationVoltage, 3)
    -255.86 * pow(compensationVoltage, 2)
    +857.39 * compensationVoltage) * 0.5;

  if (isnan(tds) || isinf(tds) || tds < 0) return NAN;

  return tds;
}

// =====================================================
// CÁLCULO TDS CALIBRADO
// =====================================================
float calculateTDS(float temperature) {
  float rawTDS = calculateRawTDS(temperature);

  if (isnan(rawTDS)) return NAN;

  float calibratedTDS = rawTDS * tdsFactor;

  if (isnan(calibratedTDS) || isinf(calibratedTDS) || calibratedTDS < 0) {
    return NAN;
  }

  return calibratedTDS;
}

// =====================================================
// PUBLICAÇÃO MQTT
// =====================================================
void publishMQTT(float temperature, float tds) {
  char tempStr[12];
  char tdsStr[12];
  char factorStr[12];

  if (!isnan(temperature)) {
    dtostrf(temperature, 1, 2, tempStr);
    mqttClient.publish(TOPIC_TEMP, tempStr);
  }

  if (!isnan(tds)) {
    dtostrf(tds, 1, 0, tdsStr);
    mqttClient.publish(TOPIC_TDS, tdsStr);
  }

  dtostrf(tdsFactor, 1, 4, factorStr);
  mqttClient.publish(TOPIC_TDS_FACTOR, factorStr);
}

// =====================================================
// SERIAL DEBUG
// =====================================================
void printDebug(float temperature, float tds) {
  int rawADC = analogRead(TDS_PIN);

  Serial.print("ADC: ");
  Serial.print(rawADC);

  Serial.print(" | Temp: ");

  if (!isnan(temperature)) {
    Serial.print(temperature, 2);
    Serial.print(" ºC");
  } else {
    Serial.print("ERRO");
  }

  Serial.print(" | TDS: ");

  if (!isnan(tds)) {
    Serial.print(tds, 0);
    Serial.print(" ppm");
  } else {
    Serial.print("ERRO");
  }

  Serial.print(" | Fator: ");
  Serial.println(tdsFactor, 4);
}

// =====================================================
// CALIBRAÇÃO VIA SERIAL
//
// Comandos:
//
// CAL:342
// Calibra usando solução conhecida de 342 ppm
//
// RESETCAL
// Apaga calibração e volta ao fator 1.0
//
// FACTOR?
// Mostra o fator atual
// =====================================================
void handleCalibration(float currentTemperature) {
  if (!Serial.available()) return;

  String cmd = Serial.readStringUntil('\n');
  cmd.trim();

  // Mostrar fator atual
  if (cmd == "FACTOR?") {
    Serial.print("Fator TDS atual: ");
    Serial.println(tdsFactor, 4);
    return;
  }

  // Reset da calibração
  if (cmd == "RESETCAL") {
    tdsFactor = 1.0;
    preferences.putFloat("factor", tdsFactor);

    Serial.println("Calibração TDS apagada.");
    Serial.println("Fator TDS reposto para 1.0000");
    return;
  }

  // Comando de calibração
  if (!cmd.startsWith("CAL:")) return;

  float realValue = cmd.substring(4).toFloat();

  // Para calibrar, usamos o TDS bruto, sem o fator anterior
  float rawTDS = calculateRawTDS(currentTemperature);

  if (realValue > 0 && !isnan(rawTDS) && rawTDS > 0) {
    tdsFactor = realValue / rawTDS;

    preferences.putFloat("factor", tdsFactor);

    Serial.println();
    Serial.println("===== CALIBRAÇÃO TDS =====");
    Serial.print("Valor real: ");
    Serial.print(realValue, 2);
    Serial.println(" ppm");

    Serial.print("Leitura bruta: ");
    Serial.print(rawTDS, 2);
    Serial.println(" ppm");

    Serial.print("Novo fator: ");
    Serial.println(tdsFactor, 4);

    Serial.println("Calibração guardada na memória.");
    Serial.println("==========================");
    Serial.println();

  } else {
    Serial.println("Erro na calibração.");
    Serial.println("Verificar:");
    Serial.println("- Sensor dentro da solução");
    Serial.println("- Temperatura válida");
    Serial.println("- Buffer de leitura já preenchido");
    Serial.println("- Valor CAL maior que zero");
  }
}

// =====================================================
// SETUP
// =====================================================
void setup() {
  Serial.begin(115200);
  delay(500);

  // ADC ESP32
  analogReadResolution(12);
  analogSetPinAttenuation(TDS_PIN, ADC_11db);

  // Memória persistente para calibração
  preferences.begin("tds", false);
  tdsFactor = preferences.getFloat("factor", 1.0);

  sensors.begin();

  setupWiFi();

  mqttClient.setServer(MQTT_SERVER, MQTT_PORT);

  Serial.println();
  Serial.println("Sistema iniciado.");
  Serial.print("Fator TDS carregado: ");
  Serial.println(tdsFactor, 4);

  Serial.println();
  Serial.println("Comandos disponíveis:");
  Serial.println("CAL:valor_real");
  Serial.println("Exemplo: CAL:342");
  Serial.println("FACTOR?");
  Serial.println("RESETCAL");
  Serial.println();
}

// =====================================================
// LOOP
// =====================================================
void loop() {
  if (!mqttClient.connected()) {
    reconnectMQTT();
  }

  mqttClient.loop();

  sampleTDS();

  static float lastTemperature = NAN;

  if (millis() - lastMainUpdate >= MAIN_INTERVAL_MS) {
    lastMainUpdate = millis();

    float temperature = readTemperature();
    float tds = calculateTDS(temperature);

    lastTemperature = temperature;

    printDebug(temperature, tds);
    publishMQTT(temperature, tds);
  }

  handleCalibration(lastTemperature);
}