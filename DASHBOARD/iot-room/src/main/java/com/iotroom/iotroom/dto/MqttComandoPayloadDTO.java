package com.iotroom.iotroom.dto;

public record MqttComandoPayloadDTO(
        Long commandId,
        Long sensorId,
        String deviceId,
        String tipoSensor,
        String comando
) {
}