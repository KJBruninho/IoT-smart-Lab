package com.iotroom.iotroom.dto;

public record MqttComandoAckDTO(
        Long commandId,
        String status,
        String message
) {
}