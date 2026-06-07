package com.iotroom.iotroom.mqtt;

import com.iotroom.iotroom.service.LeituraService;
import com.iotroom.iotroom.service.MqttStatusService;
import jakarta.annotation.PostConstruct;
import org.eclipse.paho.client.mqttv3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class MqttSubscriber {
    private final LeituraService leituraService;
    private final MqttStatusService mqttStatusService;

    @Value("${mqtt.broker}")
    private String broker;

    @Value("${mqtt.client-id}")
    private String clientId;

    @Value("${mqtt.topic-temperatura}")
    private String topicTemperatura;

    @Value("${mqtt.topic-tds}")
    private String topicTds;

    @Value("${mqtt.device-id}")
    private String deviceId;

    public MqttSubscriber(LeituraService leituraService, MqttStatusService mqttStatusService) {
        this.leituraService = leituraService;
        this.mqttStatusService = mqttStatusService;
    }

    @PostConstruct
    public void iniciar() throws MqttException {
        MqttClient client = new MqttClient(broker, clientId + "-" + System.currentTimeMillis());

        MqttConnectOptions options = new MqttConnectOptions();
        options.setAutomaticReconnect(true);
        options.setCleanSession(true);
        options.setConnectionTimeout(10);
        options.setKeepAliveInterval(20);

        client.setCallback(new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean reconnect, String serverURI) {
                mqttStatusService.marcarClienteLigado();
                try {
                    client.subscribe(topicTemperatura);
                    client.subscribe(topicTds);
                    System.out.println("MQTT subscrito em " + serverURI);
                } catch (MqttException e) {
                    System.err.println("Erro ao subscrever tópicos MQTT: " + e.getMessage());
                }
            }

            @Override
            public void connectionLost(Throwable cause) {
                mqttStatusService.marcarClienteDesligado();
                System.err.println("Ligação MQTT perdida: " + cause.getMessage());
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) {
                mqttStatusService.atualizarUltimaMensagem();

                try {
                    String payload = new String(message.getPayload()).trim();
                    BigDecimal valor = new BigDecimal(payload);

                    if (topic.equals(topicTemperatura)) {
                        leituraService.registarLeitura(deviceId, "TEMPERATURA", valor);
                    } else if (topic.equals(topicTds)) {
                        leituraService.registarLeitura(deviceId, "TDS", valor);
                    }
                } catch (Exception e) {
                    System.err.println("Erro ao processar MQTT: " + topic + " -> " + e.getMessage());
                }
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {
            }
        });

        try {
            client.connect(options);
            mqttStatusService.marcarClienteLigado();
            System.out.println("MQTT ativo em " + broker);
        } catch (MqttException e) {
            mqttStatusService.marcarClienteDesligado();
            System.err.println("MQTT indisponível no arranque. A reconexão automática fica ativa: " + e.getMessage());
        }
    }
}
