package com.iotroom.iotroom.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record AlunoPedidoModoDTO(
        Long id,
        Long sensorId,
        String sensorNome,
        String tipoSensor,
        String estacaoNome,
        String deviceId,
        String estado,
        Integer intervaloRapidoMs,
        Integer intervaloEstavelMs,
        Integer duracaoModoRapidoMs,
        BigDecimal deltaSignificativo,
        String motivo,
        String respostaProfessor,
        LocalDateTime criadoEm,
        LocalDateTime analisadoEm,
        LocalDateTime aplicadoEm
) {
}
