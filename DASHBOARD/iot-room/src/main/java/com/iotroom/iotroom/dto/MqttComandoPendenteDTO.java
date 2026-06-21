package com.iotroom.iotroom.dto;

import java.time.LocalDateTime;

public record MqttComandoPendenteDTO(
        Long id,
        Long sensorId,
        String deviceId,
        String tipoSensor,
        String comando,
        Integer tentativasEnvio,
        LocalDateTime criadoEm
) {
}