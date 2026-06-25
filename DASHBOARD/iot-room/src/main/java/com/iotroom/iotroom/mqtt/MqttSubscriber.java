package com.iotroom.iotroom.mqtt;

import com.iotroom.iotroom.service.LeituraService;
import com.iotroom.iotroom.service.MqttStatusService;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class MqttSubscriber {

    private final LeituraService leituraService;
    private final MqttStatusService mqttStatusService;

    private MqttClient client;

    @Value("${mqtt.broker:tcp://100.78.90.21:1883}")
    private String broker;

    @Value("${mqtt.client-id:iot-room-subscriber}")
    private String clientId;

    @Value("${mqtt.topic:esp32/+/leituras}")
    private String topic;

    public MqttSubscriber(
            LeituraService leituraService,
            MqttStatusService mqttStatusService
    ) {
        this.leituraService = leituraService;
        this.mqttStatusService = mqttStatusService;
    }

    @PostConstruct
    public void iniciar() {
        try {
            String clientIdUnico = clientId + "-" + System.currentTimeMillis();

            this.client = new MqttClient(broker, clientIdUnico);

            MqttConnectOptions options = new MqttConnectOptions();
            options.setAutomaticReconnect(true);
            options.setCleanSession(true);
            options.setConnectionTimeout(10);
            options.setKeepAliveInterval(20);

            client.setCallback(new MqttCallbackExtended() {
                @Override
                public void connectComplete(boolean reconnect, String serverURI) {
                    try {
                        client.subscribe(topic);
                        mqttStatusService.marcarClienteLigado();
                        System.out.println("MQTT subscrito em " + broker);
                    } catch (MqttException e) {
                        mqttStatusService.marcarClienteDesligado();
                        System.err.println("Erro ao subscrever MQTT: " + e.getMessage());
                    }
                }

                @Override
                public void connectionLost(Throwable cause) {
                    mqttStatusService.marcarClienteDesligado();
                    System.err.println("Ligação MQTT perdida: " + cause.getMessage());
                }

                @Override
                public void messageArrived(String topic, org.eclipse.paho.client.mqttv3.MqttMessage message) {
                    String payload = new String(message.getPayload());

                    try {
                        leituraService.processarMensagemMqtt(topic, payload);
                    } catch (Exception e) {
                        System.err.println("Erro ao processar mensagem MQTT: " + e.getMessage());
                    }
                }

                @Override
                public void deliveryComplete(org.eclipse.paho.client.mqttv3.IMqttDeliveryToken token) {
                    // Não usado no subscriber
                }
            });

            client.connect(options);
            mqttStatusService.marcarClienteLigado();

            System.out.println("MQTT ativo em " + broker);

        } catch (MqttException e) {
            mqttStatusService.marcarClienteDesligado();
            System.err.println("MQTT indisponível no arranque: " + e.getMessage());
        } catch (Exception e) {
            mqttStatusService.marcarClienteDesligado();
            System.err.println("Erro inesperado ao iniciar MQTT: " + e.getMessage());
        }
    }

    @PreDestroy
    public void parar() {
        try {
            if (client != null) {
                if (client.isConnected()) {
                    client.disconnect();
                }

                client.close();
                System.out.println("Cliente MQTT fechado.");
            }
        } catch (MqttException e) {
            System.err.println("Erro ao fechar cliente MQTT: " + e.getMessage());
        }
    }
}
