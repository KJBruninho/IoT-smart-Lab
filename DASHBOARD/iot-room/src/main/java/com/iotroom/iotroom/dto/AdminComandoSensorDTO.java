package com.iotroom.iotroom.dto;

import java.time.LocalDateTime;

public record AdminComandoSensorDTO(
        Long id,

        Long sensorId,
        String sensorNome,
        String tipoSensor,

        String deviceId,
        String comando,
        String estado,

        Long professorId,
        String professorNome,

        String resposta,
        String ultimoErro,

        Integer tentativasEnvio,

        LocalDateTime criadoEm,
        LocalDateTime publicadoEm,
        LocalDateTime confirmadoEm
) {
}