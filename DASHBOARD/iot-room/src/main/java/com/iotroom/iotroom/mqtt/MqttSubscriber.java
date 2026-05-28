package com.iotroom.iotroom.mqtt;

import com.iotroom.iotroom.service.LeituraService;
import jakarta.annotation.PostConstruct;
import org.eclipse.paho.client.mqttv3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class MqttSubscriber {
    private final LeituraService leituraService;

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

    public MqttSubscriber(LeituraService leituraService) {
        this.leituraService = leituraService;
    }

    @PostConstruct
    public void iniciar() throws MqttException {
        MqttClient client = new MqttClient(broker, clientId + "-" + System.currentTimeMillis());
        MqttConnectOptions options = new MqttConnectOptions();
        options.setAutomaticReconnect(true);
        options.setCleanSession(true);

        client.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable cause) {
                System.err.println("Ligação MQTT perdida: " + cause.getMessage());
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) {
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

        client.connect(options);
        client.subscribe(topicTemperatura);
        client.subscribe(topicTds);

        System.out.println("MQTT ativo em " + broker);
    }
}
