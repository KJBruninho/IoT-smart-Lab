package com.iotroom.iotroom.dto.mqtt;

public record MqttComandoAckDTO(
        Long commandId,
        String status,
        String message
) {
}