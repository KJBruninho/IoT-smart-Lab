package com.iotroom.iotroom.dto.mqtt;

public record MqttComandoPayloadDTO(
        Long commandId,
        Long sensorId,
        String deviceId,
        String tipoSensor,
        String comando
) {
}