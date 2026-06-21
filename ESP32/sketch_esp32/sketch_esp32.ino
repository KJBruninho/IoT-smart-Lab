#include <WiFi.h>
#include <PubSubClient.h>
#include <ArduinoJson.h>
#include <OneWire.h>
#include <DallasTemperature.h>
#include <Preferences.h>
#include <math.h>

// =====================================================
// IoT Room - ESP32
// =====================================================
//
// IDENTIFICAÇÃO:
//   DEVICE_ID deve coincidir com estacoes.device_id na base de dados.
//
// TÓPICOS MQTT DE DADOS PUBLICADOS PELO ESP32:
//   esp32/temperatura     -> valor final da temperatura em ºC
//   esp32/tds             -> valor final de TDS em ppm
//   esp32/ph              -> valor final de pH
//
// Nota:
//   As leituras publicadas são apenas valores finais convertidos/calibrados.
//   Não é publicado valor analógico bruto nem fator de calibração.
//   Se um sensor físico não estiver ligado ou estiver em erro, não é publicada leitura.
//   O Serial Monitor mostra N/A para esses casos.
//
// SERIAL MONITOR:
//   Mostra uma linha de debug por segundo, por exemplo:
//   ADC: 2036 | Factor: 1.0000 | Modo: RAPIDO_TEMP_TDS | MQTT: ONLINE | Temp: 29.87 °C | Envio Temp: SIM | TDS: 100 ppm | Envio TDS: SIM | pH: N/A | Envio pH: NAO
//
//   ADC e Factor aparecem apenas no Serial Monitor. Não são enviados para a BD.
//   O campo "Envio" mostra se esse sensor foi publicado desde a última linha de debug.
//   O texto digitado no Serial Monitor também pode executar comandos locais de teste.
//
// TÓPICOS MQTT DE CONTROLO:
//   esp32/{DEVICE_ID}/cmd -> backend envia comandos para o ESP32
//   esp32/{DEVICE_ID}/ack -> ESP32 confirma execução do comando
//
// Exemplo:
//   DEVICE_ID = esp32_sala_01
//   Comandos: esp32/esp32_sala_01/cmd
//   Resposta: esp32/esp32_sala_01/ack
//
// FORMATO DO COMANDO RECEBIDO:
//   {
//     "commandId": 12,
//     "sensorId": 3,
//     "deviceId": "esp32_sala_01",
//     "tipoSensor": "TDS",
//     "comando": "SET_REMOTE_ACTIVE:0"
//   }
//
// COMANDOS DISPONÍVEIS:
//
// 1) SET_REMOTE_ACTIVE:1
//    Liga o sensor indicado no campo JSON tipoSensor.
//    Se tipoSensor vier vazio, liga o processamento/publicação global.
//
// 2) SET_REMOTE_ACTIVE:0
//    Desliga o sensor indicado no campo JSON tipoSensor.
//    Se tipoSensor vier vazio, desliga o processamento/publicação global.
//
// 3) SET_SENSOR_ACTIVE:TEMPERATURA:1
//    Liga apenas o sensor de temperatura.
//
// 4) SET_SENSOR_ACTIVE:TEMPERATURA:0
//    Desliga apenas o sensor de temperatura.
//
// 5) SET_SENSOR_ACTIVE:TDS:1
//    Liga apenas o sensor TDS.
//
// 6) SET_SENSOR_ACTIVE:TDS:0
//    Desliga apenas o sensor TDS.
//
// 7) SET_SENSOR_ACTIVE:PH:1
//    Liga logicamente o sensor de pH.
//    Se phSensorConnected = false, não publica leitura de pH.
//
// 8) SET_SENSOR_ACTIVE:PH:0
//    Desliga apenas o sensor de pH.
//
// 9) SET_CONFIG:TEMPERATURA:FAST=1000;STABLE=30000;FAST_DURATION=120000;DELTA=0.20
//    Atualiza os intervalos do modo adaptativo da temperatura.
//
// 10) SET_CONFIG:TDS:FAST=1000;STABLE=30000;FAST_DURATION=120000;DELTA=5.00
//     Atualiza os intervalos do modo adaptativo do TDS.
//
// 11) SET_CONFIG:PH:FAST=1000;STABLE=30000;FAST_DURATION=120000;DELTA=0.10
//     Atualiza os intervalos do modo adaptativo do pH.
//
// 12) SET_CONFIG:FAST=1000;STABLE=30000;FAST_DURATION=120000;DELTA=5.00
//     Atualiza o sensor indicado no campo JSON tipoSensor.
//     Este formato é compatível com o backend Spring atual.
//
// 13) SET_CALIBRATION:TDS:FACTOR=1.000000
//     Atualiza o fator interno de calibração do TDS.
//     O fator não é publicado como leitura.
//
// 14) SET_CALIBRATION:PH:FACTOR=1.000000;OFFSET=0.000000
//     Atualiza a calibração interna do pH.
//     O fator e o offset não são publicados como leitura.
//
// 15) SET_CALIBRATION:FACTOR=1.000000;OFFSET=0.000000
//     Atualiza o sensor indicado no campo JSON tipoSensor.
//
// 16) SET_OFFSET:PH:-0.200000
//     Atualiza apenas o offset do pH.
//
// 17) SET_OFFSET:-0.200000
//     Atualiza apenas o offset do sensor indicado no campo JSON tipoSensor.
//
// 18) PING
//     Testa comunicação. O ESP32 responde com ACK.
//
// COMANDOS LOCAIS NO SERIAL MONITOR:
//   HELP                           -> mostra ajuda
//   STATUS                         -> mostra estado atual
//   SET_CONNECTED:PH:1             -> marca pH como fisicamente ligado
//   SET_CONNECTED:PH:0             -> marca pH como fisicamente desligado
//   SET_CONNECTED:TDS:0            -> marca TDS como fisicamente desligado
//   RESET_CALIBRATION              -> repõe calibração padrão e grava na memória
//
// Também podes escrever diretamente comandos como:
//   SET_CALIBRATION:TDS:FACTOR=0.172500
//   SET_CONFIG:TDS:FAST=1000;STABLE=30000;FAST_DURATION=120000;DELTA=5.00
//
// Ou colar o JSON completo que o backend envia.
//
// FORMATO DO ACK:
//   {
//     "commandId": 12,
//     "status": "CONFIRMADO",
//     "message": "Comando aplicado"
//   }
//
// =====================================================


// =====================================================
// IDENTIFICAÇÃO DA ESTAÇÃO
// Deve coincidir com estacoes.device_id na BD
// =====================================================
const char* DEVICE_ID = "esp32_sala_01";


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


// =====================================================
// TÓPICOS DE DADOS
// Só são enviados valores finais já convertidos/calibrados.
// =====================================================
const char* TOPIC_TEMP = "esp32/temperatura";
const char* TOPIC_TDS  = "esp32/tds";
const char* TOPIC_PH   = "esp32/ph";


// =====================================================
// TÓPICOS DE CONTROLO REMOTO
// São preenchidos automaticamente com base no DEVICE_ID.
// =====================================================
char TOPIC_CMD[96];
char TOPIC_ACK[96];
char TOPIC_STATUS[96];


// =====================================================
// PINOS
// =====================================================
#define ONE_WIRE_BUS 18
#define TDS_PIN      34
#define PH_PIN       35


// =====================================================
// CONVERSÃO INTERNA DO SINAL
// Estes valores são usados apenas para calcular TDS/pH.
// Não são publicados na BD.
// =====================================================
#define VREF    3.3
#define ADC_MAX 4095.0
#define SCOUNT  30

int tdsBuffer[SCOUNT];
int tdsBufferTemp[SCOUNT];
int tdsBufferIndex = 0;
bool tdsBufferReady = false;

int phBuffer[SCOUNT];
int phBufferTemp[SCOUNT];
int phBufferIndex = 0;
bool phBufferReady = false;

unsigned long lastAnalogSampleMs = 0;
const unsigned long ANALOG_SAMPLE_INTERVAL_MS = 40;

// Usado apenas no Serial Monitor para manter o formato de debug antigo.
int lastTdsAdcDebug = -1;
int lastPhAdcDebug = -1;
unsigned long lastDebugPrintMs = 0;
const unsigned long DEBUG_STATUS_INTERVAL_MS = 1000;

// Flags agregadas para o Serial Monitor.
// Assim o debug mostra SIM se o sensor foi enviado em qualquer momento
// desde a última linha impressa, não apenas naquele loop específico.
bool tempSentSinceLastDebug = false;
bool tdsSentSinceLastDebug = false;
bool phSentSinceLastDebug = false;

String serialInputBuffer = "";


// =====================================================
// VALOR INTERNO PARA SENSOR NÃO LIGADO / INVÁLIDO
// É usado só dentro do código e no Serial Monitor como N/A.
// Não é publicado por MQTT.
// =====================================================
const float SENSOR_NOT_CONNECTED_VALUE = -9999.0;


// =====================================================
// SENSORES FISICAMENTE LIGADOS
// O sensor de pH ainda não está ligado, por isso fica false.
// Quando ligares o módulo de pH ao GPIO 35, troca para true.
// =====================================================
bool tempSensorConnected = true;
bool tdsSensorConnected  = true;
bool phSensorConnected   = false;


// =====================================================
// CONFIGURAÇÃO DE CALIBRAÇÃO INTERNA
// Estes valores podem ser alterados por MQTT.
// Não são enviados como leituras periódicas.
// =====================================================
float tdsFactor = 1.0;
float phFactor  = 1.0;
float phOffset  = 0.0;


// =====================================================
// ESTADO LÓGICO DOS SENSORES
// false = não processa nem publica esse sensor.
// =====================================================
bool remoteEnabled = true;

bool tempEnabled = true;
bool tdsEnabled  = true;
bool phEnabled   = true;


// =====================================================
// CONFIGURAÇÃO DINÂMICA DOS MODOS
// Estes valores podem ser alterados remotamente por MQTT.
// =====================================================
unsigned long tempFastIntervalMs   = 1000;
unsigned long tempStableIntervalMs = 30000;
unsigned long tempRapidDurationMs  = 120000;
float tempDeltaLimit = 0.20;

unsigned long tdsFastIntervalMs   = 1000;
unsigned long tdsStableIntervalMs = 30000;
unsigned long tdsRapidDurationMs  = 120000;
float tdsDeltaLimit = 5.00;

unsigned long phFastIntervalMs   = 1000;
unsigned long phStableIntervalMs = 30000;
unsigned long phRapidDurationMs  = 120000;
float phDeltaLimit = 0.10;


// =====================================================
// ESTADO DO MODO ADAPTATIVO
// =====================================================
float lastTempPublished = NAN;
float lastTdsPublished  = NAN;
float lastPhPublished   = NAN;

unsigned long lastTempPublishMs = 0;
unsigned long lastTdsPublishMs  = 0;
unsigned long lastPhPublishMs   = 0;

unsigned long tempRapidUntilMs = 0;
unsigned long tdsRapidUntilMs  = 0;
unsigned long phRapidUntilMs   = 0;


// =====================================================
// CLIENTES
// =====================================================
WiFiClient espClient;
PubSubClient mqttClient(espClient);
OneWire oneWire(ONE_WIRE_BUS);
DallasTemperature tempSensor(&oneWire);
Preferences preferences;


// =====================================================
// DECLARAÇÕES
// =====================================================
void connectWiFi();
void connectMQTT();
void mqttCallback(char* topic, byte* payload, unsigned int length);
void sampleAnalogSensors();
void processSensors();
void handleSerialInput();
void processSerialLine(String line);
void printCommandHelp();
void printCurrentState();
bool setSensorPhysicalConnection(const String &sensor, bool connected, String &message);
void loadStoredSettings();
void saveCalibrationSettings();
bool publishValue(const char* topic, float value, int decimals);
void printDebugStatus(int adcTds, float temperatura, float tdsValue, float phValue, float fatorTds, const String &modoAtual, bool mqttOnline, bool enviouTemp, bool enviouTds, bool enviouPh);
String getModoAtual();
bool shouldPublish(float value, float &lastValue, unsigned long &lastPublishMs, unsigned long &rapidUntilMs,
                   unsigned long fastIntervalMs, unsigned long stableIntervalMs, unsigned long rapidDurationMs,
                   float deltaLimit);
bool isSensorErrorValue(float value);
bool isPublishableValue(float value);
float readTemperatureC();
float readTdsPpm(float temperatureC);
float readPhValue();
int getMedianNum(int bArray[], int iFilterLen);

void handleCommandJson(const String &json);
bool processCommand(unsigned long commandId, const String &tipoSensorJson, const String &command, String &message);
void sendAck(unsigned long commandId, const char* status, const String &message);

String normalizeSensor(String sensor);
bool parseSensorActiveCommand(const String &command, String &sensor, bool &enabled);
bool parseConfigCommand(const String &command, const String &tipoSensorJson, String &sensor, String &params);
bool parseCalibrationCommand(const String &command, const String &tipoSensorJson, String &sensor, String &params);
bool parseOffsetCommand(const String &command, const String &tipoSensorJson, String &sensor, float &offset);
String getParamValue(const String &params, const String &key);
bool setSensorActive(const String &sensor, bool enabled, String &message);
bool setSensorConfig(const String &sensor, const String &params, String &message);
bool setSensorCalibration(const String &sensor, const String &params, String &message);
bool setSensorOffset(const String &sensor, float offset, String &message);


void setup() {
  Serial.begin(115200);
  delay(300);

  snprintf(TOPIC_CMD, sizeof(TOPIC_CMD), "esp32/%s/cmd", DEVICE_ID);
  snprintf(TOPIC_ACK, sizeof(TOPIC_ACK), "esp32/%s/ack", DEVICE_ID);
  snprintf(TOPIC_STATUS, sizeof(TOPIC_STATUS), "esp32/%s/status", DEVICE_ID);

  analogReadResolution(12);
  analogSetAttenuation(ADC_11db);

  tempSensor.begin();

  preferences.begin("iotroom", false);
  loadStoredSettings();

  connectWiFi();

  mqttClient.setServer(MQTT_SERVER, MQTT_PORT);
  mqttClient.setCallback(mqttCallback);
  mqttClient.setBufferSize(1024);

  connectMQTT();

  Serial.println("IoT Room ESP32 iniciado.");
  Serial.println("Formato Serial: ADC | Factor | Modo | MQTT | valor de cada sensor + envio");
  Serial.print("DEVICE_ID: ");
  Serial.println(DEVICE_ID);
  Serial.print("TOPIC_CMD: ");
  Serial.println(TOPIC_CMD);
  Serial.print("TOPIC_ACK: ");
  Serial.println(TOPIC_ACK);
  Serial.println("Escreve HELP no Serial Monitor para ver comandos locais de teste.");
  printCurrentState();
}


void loop() {
  if (WiFi.status() != WL_CONNECTED) {
    connectWiFi();
  }

  if (!mqttClient.connected()) {
    connectMQTT();
  }

  mqttClient.loop();

  handleSerialInput();

  sampleAnalogSensors();
  processSensors();
}


void handleSerialInput() {
  while (Serial.available() > 0) {
    char c = (char)Serial.read();

    if (c == '\r' || c == '\n') {
      if (serialInputBuffer.length() > 0) {
        processSerialLine(serialInputBuffer);
        serialInputBuffer = "";
      }
    } else {
      serialInputBuffer += c;

      if (serialInputBuffer.length() > 900) {
        Serial.println("[SERIAL] Linha demasiado longa. Buffer limpo.");
        serialInputBuffer = "";
      }
    }
  }
}


void processSerialLine(String line) {
  line.trim();

  if (line.length() == 0) {
    return;
  }

  Serial.print("[SERIAL IN] ");
  Serial.println(line);

  if (line.equalsIgnoreCase("HELP") || line == "?") {
    printCommandHelp();
    return;
  }

  if (line.equalsIgnoreCase("STATUS")) {
    printCurrentState();
    return;
  }

  if (line.equalsIgnoreCase("RESET_CALIBRATION")) {
    tdsFactor = 1.0;
    phFactor = 1.0;
    phOffset = 0.0;
    saveCalibrationSettings();
    Serial.println("[SERIAL RESULT] Calibracao reposta para valores padrao e gravada na memoria.");
    printCurrentState();
    return;
  }

  if (line.startsWith("{")) {
    handleCommandJson(line);
    return;
  }

  if (line.startsWith("SET_CONNECTED:")) {
    String rest = line.substring(String("SET_CONNECTED:").length());
    int sep = rest.indexOf(':');

    if (sep <= 0) {
      Serial.println("[SERIAL RESULT] Formato invalido. Usa SET_CONNECTED:<SENSOR>:<0|1>.");
      return;
    }

    String sensor = normalizeSensor(rest.substring(0, sep));
    String value = rest.substring(sep + 1);
    value.trim();

    bool connected;

    if (value == "1" || value.equalsIgnoreCase("true") || value.equalsIgnoreCase("ON")) {
      connected = true;
    } else if (value == "0" || value.equalsIgnoreCase("false") || value.equalsIgnoreCase("OFF")) {
      connected = false;
    } else {
      Serial.println("[SERIAL RESULT] Valor invalido. Usa 0 ou 1.");
      return;
    }

    String message;
    bool ok = setSensorPhysicalConnection(sensor, connected, message);
    Serial.print("[SERIAL RESULT] ");
    Serial.print(ok ? "OK | " : "ERRO | ");
    Serial.println(message);
    printCurrentState();
    return;
  }

  String message;
  bool ok = processCommand(0, "", line, message);
  Serial.print("[SERIAL RESULT] ");
  Serial.print(ok ? "OK | " : "ERRO | ");
  Serial.println(message);
  printCurrentState();
}


void printCommandHelp() {
  Serial.println();
  Serial.println("===== COMANDOS SERIAL DISPONIVEIS =====");
  Serial.println("HELP");
  Serial.println("STATUS");
  Serial.println("PING");
  Serial.println("SET_REMOTE_ACTIVE:1");
  Serial.println("SET_REMOTE_ACTIVE:0");
  Serial.println("SET_SENSOR_ACTIVE:TEMPERATURA:1");
  Serial.println("SET_SENSOR_ACTIVE:TDS:0");
  Serial.println("SET_SENSOR_ACTIVE:PH:1");
  Serial.println("SET_CONNECTED:TEMPERATURA:1");
  Serial.println("SET_CONNECTED:TDS:1");
  Serial.println("SET_CONNECTED:PH:0");
  Serial.println("SET_CALIBRATION:TDS:FACTOR=0.172500");
  Serial.println("SET_CALIBRATION:PH:FACTOR=1.000000;OFFSET=-0.200000");
  Serial.println("SET_OFFSET:PH:-0.200000");
  Serial.println("SET_CONFIG:TDS:FAST=1000;STABLE=30000;FAST_DURATION=120000;DELTA=5.00");
  Serial.println("RESET_CALIBRATION");
  Serial.println();
  Serial.println("Tambem podes colar o JSON completo recebido por MQTT.");
  Serial.println("O Serial Monitor testa o ESP32 localmente. Nao envia comandos para o broker MQTT.");
  Serial.println("========================================");
  Serial.println();
}


void printCurrentState() {
  Serial.println();
  Serial.println("===== ESTADO ATUAL =====");
  Serial.print("DEVICE_ID: ");
  Serial.println(DEVICE_ID);
  Serial.print("WiFi: ");
  Serial.println(WiFi.status() == WL_CONNECTED ? "ONLINE" : "OFFLINE");
  Serial.print("MQTT: ");
  Serial.println(mqttClient.connected() ? "ONLINE" : "OFFLINE");
  Serial.print("remoteEnabled: ");
  Serial.println(remoteEnabled ? "true" : "false");

  Serial.print("Temp: fisico=");
  Serial.print(tempSensorConnected ? "SIM" : "NAO");
  Serial.print(" | logico=");
  Serial.println(tempEnabled ? "ON" : "OFF");

  Serial.print("TDS: fisico=");
  Serial.print(tdsSensorConnected ? "SIM" : "NAO");
  Serial.print(" | logico=");
  Serial.print(tdsEnabled ? "ON" : "OFF");
  Serial.print(" | factor=");
  Serial.println(tdsFactor, 6);

  Serial.print("PH: fisico=");
  Serial.print(phSensorConnected ? "SIM" : "NAO");
  Serial.print(" | logico=");
  Serial.print(phEnabled ? "ON" : "OFF");
  Serial.print(" | factor=");
  Serial.print(phFactor, 6);
  Serial.print(" | offset=");
  Serial.println(phOffset, 6);

  Serial.print("Topico CMD: ");
  Serial.println(TOPIC_CMD);
  Serial.print("Topico ACK: ");
  Serial.println(TOPIC_ACK);
  Serial.println("========================");
  Serial.println();
}


bool setSensorPhysicalConnection(const String &sensor, bool connected, String &message) {
  if (sensor == "TEMPERATURA") {
    tempSensorConnected = connected;
    message = connected ? "Sensor fisico de temperatura marcado como ligado." : "Sensor fisico de temperatura marcado como desligado.";
    return true;
  }

  if (sensor == "TDS") {
    tdsSensorConnected = connected;
    if (!connected) {
      lastTdsAdcDebug = -1;
      tdsBufferReady = false;
      tdsBufferIndex = 0;
    }
    message = connected ? "Sensor fisico TDS marcado como ligado." : "Sensor fisico TDS marcado como desligado.";
    return true;
  }

  if (sensor == "PH") {
    phSensorConnected = connected;
    if (!connected) {
      lastPhAdcDebug = -1;
      phBufferReady = false;
      phBufferIndex = 0;
    }
    message = connected ? "Sensor fisico pH marcado como ligado." : "Sensor fisico pH marcado como desligado.";
    return true;
  }

  message = "Sensor invalido em SET_CONNECTED.";
  return false;
}


void loadStoredSettings() {
  tdsFactor = preferences.getFloat("tdsFactor", tdsFactor);
  phFactor = preferences.getFloat("phFactor", phFactor);
  phOffset = preferences.getFloat("phOffset", phOffset);

  Serial.println("Calibracao carregada da memoria.");
  Serial.print("TDS factor: ");
  Serial.println(tdsFactor, 6);
  Serial.print("PH factor: ");
  Serial.println(phFactor, 6);
  Serial.print("PH offset: ");
  Serial.println(phOffset, 6);
}


void saveCalibrationSettings() {
  preferences.putFloat("tdsFactor", tdsFactor);
  preferences.putFloat("phFactor", phFactor);
  preferences.putFloat("phOffset", phOffset);
}


void connectWiFi() {
  if (WiFi.status() == WL_CONNECTED) {
    return;
  }

  Serial.print("A ligar ao WiFi: ");
  Serial.println(WIFI_SSID);

  WiFi.mode(WIFI_STA);
  WiFi.begin(WIFI_SSID, WIFI_PASSWORD);

  unsigned long start = millis();
  while (WiFi.status() != WL_CONNECTED) {
    delay(500);
    Serial.print(".");

    if (millis() - start > 30000) {
      Serial.println("\nFalha ao ligar ao WiFi. A reiniciar tentativa.");
      WiFi.disconnect();
      WiFi.begin(WIFI_SSID, WIFI_PASSWORD);
      start = millis();
    }
  }

  Serial.println("\nWiFi ligado.");
  Serial.print("IP: ");
  Serial.println(WiFi.localIP());
}


void connectMQTT() {
  while (!mqttClient.connected()) {
    Serial.print("A ligar ao MQTT: ");
    Serial.print(MQTT_SERVER);
    Serial.print(":");
    Serial.println(MQTT_PORT);

    String clientId = String("iotroom-") + DEVICE_ID + "-" + String((uint32_t)ESP.getEfuseMac(), HEX);

    if (mqttClient.connect(clientId.c_str(), TOPIC_STATUS, 1, true, "OFFLINE")) {
      Serial.println("MQTT ligado.");

      mqttClient.subscribe(TOPIC_CMD, 1);
      mqttClient.publish(TOPIC_STATUS, "ONLINE", true);

      Serial.print("Subscrito em: ");
      Serial.println(TOPIC_CMD);
    } else {
      Serial.print("Falha MQTT. Código: ");
      Serial.println(mqttClient.state());
      delay(3000);
    }
  }
}


void mqttCallback(char* topic, byte* payload, unsigned int length) {
  String topicString = String(topic);
  String json;
  json.reserve(length + 1);

  for (unsigned int i = 0; i < length; i++) {
    json += (char)payload[i];
  }

  Serial.print("[MQTT IN] ");
  Serial.print(topicString);
  Serial.print(" ");
  Serial.println(json);

  if (topicString == String(TOPIC_CMD)) {
    handleCommandJson(json);
  }
}


void sampleAnalogSensors() {
  unsigned long now = millis();

  if (now - lastAnalogSampleMs < ANALOG_SAMPLE_INTERVAL_MS) {
    return;
  }

  lastAnalogSampleMs = now;

  if (tdsSensorConnected) {
    tdsBuffer[tdsBufferIndex] = analogRead(TDS_PIN);
    tdsBufferIndex++;

    if (tdsBufferIndex >= SCOUNT) {
      tdsBufferIndex = 0;
      tdsBufferReady = true;
    }
  }

  if (phSensorConnected) {
    phBuffer[phBufferIndex] = analogRead(PH_PIN);
    phBufferIndex++;

    if (phBufferIndex >= SCOUNT) {
      phBufferIndex = 0;
      phBufferReady = true;
    }
  }
}


void processSensors() {
  unsigned long now = millis();

  bool enviouTemp = false;
  bool enviouTds = false;
  bool enviouPh = false;

  float temperatureC = SENSOR_NOT_CONNECTED_VALUE;
  float tdsPpm = SENSOR_NOT_CONNECTED_VALUE;
  float phValue = SENSOR_NOT_CONNECTED_VALUE;

  if (remoteEnabled && tempEnabled) {
    temperatureC = readTemperatureC();
  }

  if (remoteEnabled && tdsEnabled) {
    // O TDS usa a temperatura para compensação. Se a temperatura estiver inválida,
    // readTdsPpm assume 25 ºC internamente.
    tdsPpm = readTdsPpm(temperatureC);
  }

  if (remoteEnabled && phEnabled) {
    phValue = readPhValue();
  }

  if (remoteEnabled && tempEnabled && isPublishableValue(temperatureC)) {
    if (shouldPublish(
          temperatureC,
          lastTempPublished,
          lastTempPublishMs,
          tempRapidUntilMs,
          tempFastIntervalMs,
          tempStableIntervalMs,
          tempRapidDurationMs,
          tempDeltaLimit
        )) {
      enviouTemp = publishValue(TOPIC_TEMP, temperatureC, 2);
      if (enviouTemp) {
        tempSentSinceLastDebug = true;
      }
    }
  }

  if (remoteEnabled && tdsEnabled && isPublishableValue(tdsPpm)) {
    if (shouldPublish(
          tdsPpm,
          lastTdsPublished,
          lastTdsPublishMs,
          tdsRapidUntilMs,
          tdsFastIntervalMs,
          tdsStableIntervalMs,
          tdsRapidDurationMs,
          tdsDeltaLimit
        )) {
      enviouTds = publishValue(TOPIC_TDS, tdsPpm, 0);
      if (enviouTds) {
        tdsSentSinceLastDebug = true;
      }
    }
  }

  if (remoteEnabled && phEnabled && isPublishableValue(phValue)) {
    if (shouldPublish(
          phValue,
          lastPhPublished,
          lastPhPublishMs,
          phRapidUntilMs,
          phFastIntervalMs,
          phStableIntervalMs,
          phRapidDurationMs,
          phDeltaLimit
        )) {
      enviouPh = publishValue(TOPIC_PH, phValue, 2);
      if (enviouPh) {
        phSentSinceLastDebug = true;
      }
    }
  }

  if (now - lastDebugPrintMs >= DEBUG_STATUS_INTERVAL_MS) {
    lastDebugPrintMs = now;

    printDebugStatus(
      lastTdsAdcDebug,
      temperatureC,
      tdsPpm,
      phValue,
      tdsFactor,
      getModoAtual(),
      mqttClient.connected(),
      tempSentSinceLastDebug,
      tdsSentSinceLastDebug,
      phSentSinceLastDebug
    );

    tempSentSinceLastDebug = false;
    tdsSentSinceLastDebug = false;
    phSentSinceLastDebug = false;
  }
}

bool shouldPublish(float value, float &lastValue, unsigned long &lastPublishMs, unsigned long &rapidUntilMs,
                   unsigned long fastIntervalMs, unsigned long stableIntervalMs, unsigned long rapidDurationMs,
                   float deltaLimit) {
  unsigned long now = millis();

  if (isSensorErrorValue(value)) {
    if (isnan(lastValue) || !isSensorErrorValue(lastValue) || now - lastPublishMs >= stableIntervalMs) {
      lastValue = value;
      lastPublishMs = now;
      return true;
    }

    return false;
  }

  if (isnan(lastValue) || isSensorErrorValue(lastValue)) {
    lastValue = value;
    lastPublishMs = now;
    rapidUntilMs = now + rapidDurationMs;
    return true;
  }

  float diff = fabs(value - lastValue);

  if (diff >= deltaLimit) {
    rapidUntilMs = now + rapidDurationMs;

    if (now - lastPublishMs >= fastIntervalMs) {
      lastValue = value;
      lastPublishMs = now;
      return true;
    }

    return false;
  }

  unsigned long interval = now < rapidUntilMs ? fastIntervalMs : stableIntervalMs;

  if (now - lastPublishMs >= interval) {
    lastValue = value;
    lastPublishMs = now;
    return true;
  }

  return false;
}


bool publishValue(const char* topic, float value, int decimals) {
  if (!mqttClient.connected()) {
    return false;
  }

  if (!isPublishableValue(value)) {
    return false;
  }

  char payload[24];
  dtostrf(value, 0, decimals, payload);

  bool ok = mqttClient.publish(topic, payload, false);

  return ok;
}


void printDebugStatus(int adcTds, float temperatura, float tdsValue, float phValue, float fatorTds, const String &modoAtual, bool mqttOnline, bool enviouTemp, bool enviouTds, bool enviouPh) {
  Serial.print("ADC: ");
  if (adcTds >= 0) {
    Serial.print(adcTds);
  } else {
    Serial.print("-");
  }

  Serial.print(" | Factor: ");
  Serial.print(fatorTds, 4);

  Serial.print(" | Modo: ");
  Serial.print(modoAtual);

  Serial.print(" | MQTT: ");
  Serial.print(mqttOnline ? "ONLINE" : "OFFLINE");

  Serial.print(" | Temp: ");
  if (isnan(temperatura)) {
    Serial.print("-");
  } else if (isSensorErrorValue(temperatura)) {
    Serial.print("N/A");
  } else {
    Serial.print(temperatura, 2);
  }
  Serial.print(" °C");
  Serial.print(" | Envio Temp: ");
  Serial.print(enviouTemp ? "SIM" : "NAO");

  Serial.print(" | TDS: ");
  if (isnan(tdsValue)) {
    Serial.print("-");
  } else if (isSensorErrorValue(tdsValue)) {
    Serial.print("N/A");
  } else {
    Serial.print(tdsValue, 0);
  }
  Serial.print(" ppm");
  Serial.print(" | Envio TDS: ");
  Serial.print(enviouTds ? "SIM" : "NAO");

  Serial.print(" | pH: ");
  if (isnan(phValue)) {
    Serial.print("-");
  } else if (isSensorErrorValue(phValue)) {
    Serial.print("N/A");
  } else {
    Serial.print(phValue, 2);
  }
  Serial.print(" | Envio pH: ");
  Serial.println(enviouPh ? "SIM" : "NAO");
}



String getModoAtual() {
  if (!remoteEnabled) {
    return "REMOTO_OFF";
  }

  unsigned long now = millis();
  bool tempRapido = now < tempRapidUntilMs;
  bool tdsRapido = now < tdsRapidUntilMs;
  bool phRapido = now < phRapidUntilMs;

  if (!tempRapido && !tdsRapido && !phRapido) {
    return "ESTAVEL";
  }

  String modo = "RAPIDO";

  if (tempRapido) {
    modo += "_TEMP";
  }

  if (tdsRapido) {
    modo += "_TDS";
  }

  if (phRapido) {
    modo += "_PH";
  }

  return modo;
}

bool isSensorErrorValue(float value) {
  return value == SENSOR_NOT_CONNECTED_VALUE;
}


bool isPublishableValue(float value) {
  return !isnan(value) && !isSensorErrorValue(value);
}


float readTemperatureC() {
  if (!tempSensorConnected) {
    return SENSOR_NOT_CONNECTED_VALUE;
  }

  tempSensor.requestTemperatures();
  float temperatureC = tempSensor.getTempCByIndex(0);

  if (temperatureC == DEVICE_DISCONNECTED_C || temperatureC < -55 || temperatureC > 125) {
    return SENSOR_NOT_CONNECTED_VALUE;
  }

  return temperatureC;
}


float readTdsPpm(float temperatureC) {
  if (!tdsSensorConnected) {
    lastTdsAdcDebug = -1;
    return SENSOR_NOT_CONNECTED_VALUE;
  }

  if (!tdsBufferReady) {
    return NAN;
  }

  for (int i = 0; i < SCOUNT; i++) {
    tdsBufferTemp[i] = tdsBuffer[i];
  }

  int medianValue = getMedianNum(tdsBufferTemp, SCOUNT);
  lastTdsAdcDebug = medianValue;
  float voltage = medianValue * VREF / ADC_MAX;

  if (isnan(temperatureC) || isSensorErrorValue(temperatureC)) {
    temperatureC = 25.0;
  }

  float compensationCoefficient = 1.0 + 0.02 * (temperatureC - 25.0);
  float compensationVoltage = voltage / compensationCoefficient;

  float tdsValue = (
      133.42 * compensationVoltage * compensationVoltage * compensationVoltage
      - 255.86 * compensationVoltage * compensationVoltage
      + 857.39 * compensationVoltage
    ) * 0.5;

  tdsValue = tdsValue * tdsFactor;

  if (tdsValue < 0) {
    tdsValue = 0;
  }

  return tdsValue;
}


float readPhValue() {
  if (!phSensorConnected) {
    lastPhAdcDebug = -1;
    return SENSOR_NOT_CONNECTED_VALUE;
  }

  if (!phBufferReady) {
    return NAN;
  }

  for (int i = 0; i < SCOUNT; i++) {
    phBufferTemp[i] = phBuffer[i];
  }

  int medianValue = getMedianNum(phBufferTemp, SCOUNT);
  lastPhAdcDebug = medianValue;
  float voltage = medianValue * VREF / ADC_MAX;

  // Se a tensão estiver encostada aos limites, trata como sensor ausente/inválido.
  if (voltage < 0.05 || voltage > 3.25) {
    return SENSOR_NOT_CONNECTED_VALUE;
  }

  // Fórmula genérica para módulo de pH analógico.
  // Ajusta phFactor/phOffset por calibração real.
  float phValue = 7.0 + ((2.5 - voltage) / 0.18);
  phValue = (phValue * phFactor) + phOffset;

  if (phValue < 0 || phValue > 14) {
    return SENSOR_NOT_CONNECTED_VALUE;
  }

  return phValue;
}


int getMedianNum(int bArray[], int iFilterLen) {
  int bTab[iFilterLen];

  for (byte i = 0; i < iFilterLen; i++) {
    bTab[i] = bArray[i];
  }

  int i, j, bTemp;

  for (j = 0; j < iFilterLen - 1; j++) {
    for (i = 0; i < iFilterLen - j - 1; i++) {
      if (bTab[i] > bTab[i + 1]) {
        bTemp = bTab[i];
        bTab[i] = bTab[i + 1];
        bTab[i + 1] = bTemp;
      }
    }
  }

  if ((iFilterLen & 1) > 0) {
    return bTab[(iFilterLen - 1) / 2];
  }

  return (bTab[iFilterLen / 2] + bTab[iFilterLen / 2 - 1]) / 2;
}


void handleCommandJson(const String &json) {
  StaticJsonDocument<768> doc;
  DeserializationError error = deserializeJson(doc, json);

  if (error) {
    Serial.print("JSON inválido: ");
    Serial.println(error.c_str());
    return;
  }

  unsigned long commandId = doc["commandId"] | 0;
  const char* deviceFromPayload = doc["deviceId"] | "";
  const char* tipoSensor = doc["tipoSensor"] | "";
  const char* comando = doc["comando"] | "";

  if (strlen(deviceFromPayload) > 0 && String(deviceFromPayload) != String(DEVICE_ID)) {
    Serial.println("Comando ignorado: deviceId não corresponde a esta estação.");
    return;
  }

  String message;
  bool ok = processCommand(commandId, String(tipoSensor), String(comando), message);

  sendAck(commandId, ok ? "CONFIRMADO" : "ERRO", message);
}


bool processCommand(unsigned long commandId, const String &tipoSensorJson, const String &commandRaw, String &message) {
  String command = commandRaw;
  command.trim();

  Serial.print("[CMD] commandId=");
  Serial.print(commandId);
  Serial.print(" | tipoSensor=");
  Serial.print(tipoSensorJson.length() > 0 ? tipoSensorJson : "-");
  Serial.print(" | comando=");
  Serial.println(command);

  if (command.length() == 0) {
    message = "Comando vazio.";
    return false;
  }

  if (command == "PING") {
    message = "PONG";
    return true;
  }

  if (command.startsWith("SET_REMOTE_ACTIVE:")) {
    String value = command.substring(String("SET_REMOTE_ACTIVE:").length());
    value.trim();

    bool enabled;

    if (value == "1" || value.equalsIgnoreCase("true") || value.equalsIgnoreCase("ON")) {
      enabled = true;
    } else if (value == "0" || value.equalsIgnoreCase("false") || value.equalsIgnoreCase("OFF")) {
      enabled = false;
    } else {
      message = "Valor inválido para SET_REMOTE_ACTIVE.";
      return false;
    }

    String sensorFromPayload = normalizeSensor(tipoSensorJson);

    if (sensorFromPayload == "TEMPERATURA" || sensorFromPayload == "TDS" || sensorFromPayload == "PH") {
      return setSensorActive(sensorFromPayload, enabled, message);
    }

    bool previous = remoteEnabled;
    remoteEnabled = enabled;
    Serial.print("[REMOTE] Global anterior: ");
    Serial.print(previous ? "ON" : "OFF");
    Serial.print(" | novo: ");
    Serial.println(remoteEnabled ? "ON" : "OFF");
    message = enabled ? "Processamento global ligado." : "Processamento global desligado.";
    return true;
  }

  if (command.startsWith("SET_SENSOR_ACTIVE:")) {
    String sensor;
    bool enabled;

    if (!parseSensorActiveCommand(command, sensor, enabled)) {
      message = "Formato inválido. Usa SET_SENSOR_ACTIVE:<SENSOR>:<0|1>.";
      return false;
    }

    return setSensorActive(sensor, enabled, message);
  }

  if (command.startsWith("SET_CONFIG:")) {
    String sensor;
    String params;

    if (!parseConfigCommand(command, tipoSensorJson, sensor, params)) {
      message = "Formato inválido para SET_CONFIG.";
      return false;
    }

    return setSensorConfig(sensor, params, message);
  }

  if (command.startsWith("SET_CALIBRATION:")) {
    String sensor;
    String params;

    if (!parseCalibrationCommand(command, tipoSensorJson, sensor, params)) {
      message = "Formato inválido para SET_CALIBRATION.";
      return false;
    }

    return setSensorCalibration(sensor, params, message);
  }

  if (command.startsWith("SET_OFFSET:")) {
    String sensor;
    float offset;

    if (!parseOffsetCommand(command, tipoSensorJson, sensor, offset)) {
      message = "Formato inválido para SET_OFFSET.";
      return false;
    }

    return setSensorOffset(sensor, offset, message);
  }

  message = "Comando não reconhecido.";
  return false;
}


void sendAck(unsigned long commandId, const char* status, const String &message) {
  if (!mqttClient.connected()) {
    return;
  }

  StaticJsonDocument<384> doc;
  doc["commandId"] = commandId;
  doc["status"] = status;
  doc["message"] = message;

  char payload[384];
  size_t len = serializeJson(doc, payload, sizeof(payload));

  bool ok = mqttClient.publish(TOPIC_ACK, payload, false);

  Serial.print("[MQTT ACK] ");
  Serial.print(TOPIC_ACK);
  Serial.print(" ");
  Serial.print(payload);
  Serial.print(" ");
  Serial.println(ok ? "OK" : "ERRO");
}


String normalizeSensor(String sensor) {
  sensor.trim();
  sensor.toUpperCase();

  if (sensor == "TEMPERATURE") {
    return "TEMPERATURA";
  }

  if (sensor == "PH" || sensor == "P_H" || sensor == "P-H") {
    return "PH";
  }

  return sensor;
}


bool parseSensorActiveCommand(const String &command, String &sensor, bool &enabled) {
  String rest = command.substring(String("SET_SENSOR_ACTIVE:").length());
  int sep = rest.indexOf(':');

  if (sep <= 0) {
    return false;
  }

  sensor = normalizeSensor(rest.substring(0, sep));
  String value = rest.substring(sep + 1);
  value.trim();

  if (value == "1" || value.equalsIgnoreCase("true") || value.equalsIgnoreCase("ON")) {
    enabled = true;
    return true;
  }

  if (value == "0" || value.equalsIgnoreCase("false") || value.equalsIgnoreCase("OFF")) {
    enabled = false;
    return true;
  }

  return false;
}


bool parseConfigCommand(const String &command, const String &tipoSensorJson, String &sensor, String &params) {
  String rest = command.substring(String("SET_CONFIG:").length());
  int sep = rest.indexOf(':');

  if (sep > 0) {
    String possibleSensor = normalizeSensor(rest.substring(0, sep));

    if (possibleSensor == "TEMPERATURA" || possibleSensor == "TDS" || possibleSensor == "PH") {
      sensor = possibleSensor;
      params = rest.substring(sep + 1);
      params.trim();
      return params.length() > 0;
    }
  }

  sensor = normalizeSensor(tipoSensorJson);
  params = rest;
  params.trim();

  return (sensor == "TEMPERATURA" || sensor == "TDS" || sensor == "PH") && params.length() > 0;
}


bool parseCalibrationCommand(const String &command, const String &tipoSensorJson, String &sensor, String &params) {
  String rest = command.substring(String("SET_CALIBRATION:").length());
  int sep = rest.indexOf(':');

  if (sep > 0) {
    String possibleSensor = normalizeSensor(rest.substring(0, sep));

    if (possibleSensor == "TDS" || possibleSensor == "PH") {
      sensor = possibleSensor;
      params = rest.substring(sep + 1);
      params.trim();
      return params.length() > 0;
    }
  }

  sensor = normalizeSensor(tipoSensorJson);
  params = rest;
  params.trim();

  return (sensor == "TDS" || sensor == "PH") && params.length() > 0;
}


bool parseOffsetCommand(const String &command, const String &tipoSensorJson, String &sensor, float &offset) {
  String rest = command.substring(String("SET_OFFSET:").length());
  int sep = rest.indexOf(':');

  if (sep > 0) {
    String possibleSensor = normalizeSensor(rest.substring(0, sep));

    if (possibleSensor == "PH") {
      sensor = possibleSensor;
      String value = rest.substring(sep + 1);
      value.trim();
      offset = value.toFloat();
      return true;
    }
  }

  sensor = normalizeSensor(tipoSensorJson);
  rest.trim();
  offset = rest.toFloat();

  return sensor == "PH" && rest.length() > 0;
}


String getParamValue(const String &params, const String &key) {
  String search = key + "=";
  int start = params.indexOf(search);

  if (start < 0) {
    return "";
  }

  start += search.length();
  int end = params.indexOf(';', start);

  if (end < 0) {
    end = params.length();
  }

  String value = params.substring(start, end);
  value.trim();
  return value;
}


bool setSensorActive(const String &sensor, bool enabled, String &message) {
  if (sensor == "TEMPERATURA") {
    bool previous = tempEnabled;
    tempEnabled = enabled;
    Serial.print("[SENSOR ACTIVE] TEMPERATURA anterior: ");
    Serial.print(previous ? "ON" : "OFF");
    Serial.print(" | novo: ");
    Serial.println(tempEnabled ? "ON" : "OFF");
    message = enabled ? "Sensor de temperatura ligado." : "Sensor de temperatura desligado.";
    return true;
  }

  if (sensor == "TDS") {
    bool previous = tdsEnabled;
    tdsEnabled = enabled;
    Serial.print("[SENSOR ACTIVE] TDS anterior: ");
    Serial.print(previous ? "ON" : "OFF");
    Serial.print(" | novo: ");
    Serial.println(tdsEnabled ? "ON" : "OFF");
    message = enabled ? "Sensor TDS ligado." : "Sensor TDS desligado.";
    return true;
  }

  if (sensor == "PH") {
    bool previous = phEnabled;
    phEnabled = enabled;
    Serial.print("[SENSOR ACTIVE] PH anterior: ");
    Serial.print(previous ? "ON" : "OFF");
    Serial.print(" | novo: ");
    Serial.println(phEnabled ? "ON" : "OFF");
    message = enabled ? "Sensor de pH ligado." : "Sensor de pH desligado.";
    return true;
  }

  message = "Sensor inválido.";
  return false;
}


bool setSensorConfig(const String &sensor, const String &params, String &message) {
  String fastValue = getParamValue(params, "FAST");
  String stableValue = getParamValue(params, "STABLE");
  String durationValue = getParamValue(params, "FAST_DURATION");
  String deltaValue = getParamValue(params, "DELTA");

  if (fastValue.length() == 0 || stableValue.length() == 0 || durationValue.length() == 0 || deltaValue.length() == 0) {
    message = "Parâmetros obrigatórios: FAST, STABLE, FAST_DURATION e DELTA.";
    return false;
  }

  unsigned long fastMs = fastValue.toInt();
  unsigned long stableMs = stableValue.toInt();
  unsigned long rapidDurationMs = durationValue.toInt();
  float delta = deltaValue.toFloat();

  if (fastMs < 1000 || stableMs < 1000 || rapidDurationMs < 1000 || delta < 0) {
    message = "Valores inválidos na configuração do modo adaptativo.";
    return false;
  }

  if (sensor == "TEMPERATURA") {
    tempFastIntervalMs = fastMs;
    tempStableIntervalMs = stableMs;
    tempRapidDurationMs = rapidDurationMs;
    tempDeltaLimit = delta;
    Serial.print("[CONFIG] TEMPERATURA | FAST=");
    Serial.print(tempFastIntervalMs);
    Serial.print(" | STABLE=");
    Serial.print(tempStableIntervalMs);
    Serial.print(" | FAST_DURATION=");
    Serial.print(tempRapidDurationMs);
    Serial.print(" | DELTA=");
    Serial.println(tempDeltaLimit, 4);
    message = "Configuração da temperatura atualizada.";
    return true;
  }

  if (sensor == "TDS") {
    tdsFastIntervalMs = fastMs;
    tdsStableIntervalMs = stableMs;
    tdsRapidDurationMs = rapidDurationMs;
    tdsDeltaLimit = delta;
    Serial.print("[CONFIG] TDS | FAST=");
    Serial.print(tdsFastIntervalMs);
    Serial.print(" | STABLE=");
    Serial.print(tdsStableIntervalMs);
    Serial.print(" | FAST_DURATION=");
    Serial.print(tdsRapidDurationMs);
    Serial.print(" | DELTA=");
    Serial.println(tdsDeltaLimit, 4);
    message = "Configuração do TDS atualizada.";
    return true;
  }

  if (sensor == "PH") {
    phFastIntervalMs = fastMs;
    phStableIntervalMs = stableMs;
    phRapidDurationMs = rapidDurationMs;
    phDeltaLimit = delta;
    Serial.print("[CONFIG] PH | FAST=");
    Serial.print(phFastIntervalMs);
    Serial.print(" | STABLE=");
    Serial.print(phStableIntervalMs);
    Serial.print(" | FAST_DURATION=");
    Serial.print(phRapidDurationMs);
    Serial.print(" | DELTA=");
    Serial.println(phDeltaLimit, 4);
    message = "Configuração do pH atualizada.";
    return true;
  }

  message = "Sensor inválido para SET_CONFIG.";
  return false;
}


bool setSensorCalibration(const String &sensor, const String &params, String &message) {
  String factorValue = getParamValue(params, "FACTOR");
  String offsetValue = getParamValue(params, "OFFSET");

  if (sensor == "TDS") {
    if (factorValue.length() == 0) {
      message = "FACTOR é obrigatório para calibrar TDS.";
      return false;
    }

    float factor = factorValue.toFloat();

    if (factor <= 0) {
      message = "FACTOR inválido para TDS.";
      return false;
    }

    float previous = tdsFactor;
    tdsFactor = factor;
    saveCalibrationSettings();

    Serial.print("[CALIBRACAO] TDS | Factor anterior: ");
    Serial.print(previous, 6);
    Serial.print(" | Novo factor: ");
    Serial.println(tdsFactor, 6);

    message = "Calibração do TDS atualizada.";
    return true;
  }

  if (sensor == "PH") {
    bool changed = false;
    float previousFactor = phFactor;
    float previousOffset = phOffset;

    if (factorValue.length() > 0) {
      float factor = factorValue.toFloat();

      if (factor <= 0) {
        message = "FACTOR inválido para pH.";
        return false;
      }

      phFactor = factor;
      changed = true;
    }

    if (offsetValue.length() > 0) {
      phOffset = offsetValue.toFloat();
      changed = true;
    }

    if (!changed) {
      message = "Indica FACTOR e/ou OFFSET para calibrar pH.";
      return false;
    }

    saveCalibrationSettings();

    Serial.print("[CALIBRACAO] PH | Factor anterior: ");
    Serial.print(previousFactor, 6);
    Serial.print(" | Novo factor: ");
    Serial.print(phFactor, 6);
    Serial.print(" | Offset anterior: ");
    Serial.print(previousOffset, 6);
    Serial.print(" | Novo offset: ");
    Serial.println(phOffset, 6);

    message = "Calibração do pH atualizada.";
    return true;
  }

  message = "Sensor inválido para calibração.";
  return false;
}


bool setSensorOffset(const String &sensor, float offset, String &message) {
  if (sensor != "PH") {
    message = "SET_OFFSET só é suportado para PH.";
    return false;
  }

  float previous = phOffset;
  phOffset = offset;
  saveCalibrationSettings();

  Serial.print("[CALIBRACAO] PH | Offset anterior: ");
  Serial.print(previous, 6);
  Serial.print(" | Novo offset: ");
  Serial.println(phOffset, 6);

  message = "Offset do pH atualizado.";
  return true;
}
