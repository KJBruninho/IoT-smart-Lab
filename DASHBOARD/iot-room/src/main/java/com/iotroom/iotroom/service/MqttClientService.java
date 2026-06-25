package com.iotroom.iotroom.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.iotroom.iotroom.dto.MqttComandoAckDTO;
import org.eclipse.paho.client.mqttv3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Service
public class MqttClientService {

    private static final Logger logger = LoggerFactory.getLogger(MqttClientService.class);

    private final MqttConfiguracaoService mqttConfiguracaoService;
    private final MqttComandoAckService mqttComandoAckService;
    private final ObjectMapper objectMapper;

    private MqttClient client;
    private String brokerAtual;

    public MqttClientService(
            MqttConfiguracaoService mqttConfiguracaoService,
            MqttComandoAckService mqttComandoAckService,
            ObjectMapper objectMapper
    ) {
        this.mqttConfiguracaoService = mqttConfiguracaoService;
        this.mqttComandoAckService = mqttComandoAckService;
        this.objectMapper = objectMapper;
    }

    @PostConstruct
    public void iniciar() {
        try {
            garantirLigacao();
        } catch (Exception e) {
            logger.error("[MQTT] Falha ao iniciar cliente MQTT: {}", e.getMessage(), e);
        }
    }

    @PreDestroy
    public void fechar() {
        try {
            if (client != null && client.isConnected()) {
                logger.info("[MQTT] A desligar cliente MQTT.");
                client.disconnect();
                client.close();
            }
        } catch (Exception e) {
            logger.warn("[MQTT] Erro ao fechar cliente MQTT: {}", e.getMessage());
        }
    }

    public synchronized void publicarComando(String deviceId, String payloadJson) {
        garantirLigacao();

        String topico = mqttConfiguracaoService.getTopicoBase() + "/" + deviceId + "/cmd";

        try {
            MqttMessage message = new MqttMessage(payloadJson.getBytes(StandardCharsets.UTF_8));
            message.setQos(1);
            message.setRetained(false);

            logger.info("[MQTT OUT] Topico={} Payload={}", topico, payloadJson);

            client.publish(topico, message);

            logger.info("[MQTT OUT OK] Comando publicado em {}", topico);
        } catch (Exception e) {
            logger.error("[MQTT OUT ERRO] Falha ao publicar em {}: {}", topico, e.getMessage(), e);
            throw new IllegalStateException("Falha ao publicar comando MQTT: " + e.getMessage(), e);
        }
    }

    private synchronized void garantirLigacao() {
        String broker = mqttConfiguracaoService.getBrokerUrl();

        try {
            if (client != null && client.isConnected() && broker.equals(brokerAtual)) {
                return;
            }

            if (client != null) {
                try {
                    logger.info("[MQTT] Broker alterado ou ligação perdida. A fechar cliente anterior.");
                    client.disconnect();
                    client.close();
                } catch (Exception ignored) {
                }
            }

            brokerAtual = broker;

            String clientId = "iot-room-backend-" + UUID.randomUUID();

            logger.info("[MQTT] A ligar ao broker {} com clientId={}", broker, clientId);

            client = new MqttClient(broker, clientId, null);
            client.setCallback(criarCallback());

            MqttConnectOptions options = new MqttConnectOptions();
            options.setAutomaticReconnect(true);
            options.setCleanSession(true);
            options.setConnectionTimeout(10);
            options.setKeepAliveInterval(30);

            client.connect(options);

            logger.info("[MQTT] Ligação estabelecida com sucesso.");

            String ackTopic = mqttConfiguracaoService.getTopicoBase() + "/+/ack";

            client.subscribe(ackTopic, 1);

            logger.info("[MQTT SUB] Subscrito em {}", ackTopic);

        } catch (Exception e) {
            logger.error("[MQTT] Falha na ligação MQTT ao broker {}: {}", broker, e.getMessage(), e);
            throw new IllegalStateException("Falha na ligação MQTT: " + e.getMessage(), e);
        }
    }

    private MqttCallback criarCallback() {
        return new MqttCallback() {
            @Override
            public void connectionLost(Throwable cause) {
                if (cause != null) {
                    logger.warn("[MQTT] Ligação perdida: {}", cause.getMessage());
                } else {
                    logger.warn("[MQTT] Ligação perdida.");
                }
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) {
                String payload = new String(message.getPayload(), StandardCharsets.UTF_8);

                logger.info("[MQTT IN] Topico={} Payload={}", topic, payload);

                try {
                    if (topic.endsWith("/ack")) {
                        MqttComandoAckDTO ack = objectMapper.readValue(payload, MqttComandoAckDTO.class);

                        logger.info(
                                "[MQTT ACK RECEBIDO] commandId={} status={} message={}",
                                ack.commandId(),
                                ack.status(),
                                ack.message()
                        );

                        mqttComandoAckService.processarAck(ack);
                        return;
                    }

                    logger.warn("[MQTT IN IGNORADO] Tópico não reconhecido: {}", topic);

                } catch (Exception e) {
                    logger.error("[MQTT IN ERRO] Erro ao processar mensagem em {}: {}", topic, e.getMessage(), e);
                }
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {
                try {
                    logger.debug("[MQTT DELIVERY] Publicação entregue localmente. MessageId={}", token.getMessageId());
                } catch (Exception ignored) {
                }
            }
        };
    }
}