package com.iotroom.iotroom.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PedidoConfiguracaoSensorDTO(
        Long id,
        Long sensorId,
        String sensorNome,
        String sensorTipo,
        String estacao,
        Long solicitadoPor,
        Long analisadoPor,
        String origem,
        String estado,
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