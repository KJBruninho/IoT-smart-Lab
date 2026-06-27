package com.iotroom.iotroom.mqtt;

import org.eclipse.paho.client.mqttv3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class MqttCommandPublisher {

    @Value("${mqtt.broker}")
    private String broker;

    @Value("${mqtt.command-prefix:iotroom}")
    private String commandPrefix;

    public void enviarComando(String deviceId, String comando) {
        String topic = commandPrefix + "/" + deviceId + "/cmd";

        String clientId = "iot-room-command-" + System.currentTimeMillis();

        try {
            MqttClient client = new MqttClient(broker, clientId);

            MqttConnectOptions options = new MqttConnectOptions();
            options.setCleanSession(true);
            options.setConnectionTimeout(5);

            client.connect(options);

            MqttMessage message = new MqttMessage(comando.getBytes());
            message.setQos(1);
            message.setRetained(false);

            client.publish(topic, message);
            client.disconnect();
            client.close();

            System.out.println("Comando enviado: " + topic + " -> " + comando);

        } catch (MqttException e) {
            throw new IllegalStateException("Erro ao enviar comando MQTT: " + e.getMessage(), e);
        }
    }
}