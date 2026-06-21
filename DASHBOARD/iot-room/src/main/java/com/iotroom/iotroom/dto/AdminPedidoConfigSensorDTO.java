package com.iotroom.iotroom.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record AdminPedidoConfigSensorDTO(
        Long id,

        Long sensorId,
        String sensorNome,
        String tipoSensor,
        String unidade,

        Long estacaoId,
        String estacaoNome,
        String deviceId,

        String origem,
        String estado,

        Long solicitadoPorId,
        String solicitadoPorNome,

        Long analisadoPorId,
        String analisadoPorNome,

        Integer intervaloRapidoMs,
        Integer intervaloEstavelMs,
        Integer duracaoModoRapidoMs,
        BigDecimal deltaSignificativo,

        String motivo,
        String respostaProfessor,

        Long comandoId,

        LocalDateTime criadoEm,
        LocalDateTime analisadoEm,
        LocalDateTime aplicadoEm
) {
}