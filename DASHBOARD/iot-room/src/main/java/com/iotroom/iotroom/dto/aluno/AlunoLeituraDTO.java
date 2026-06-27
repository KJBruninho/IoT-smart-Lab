package com.iotroom.iotroom.dto.aluno;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record AlunoLeituraDTO(
        Long leituraId,
        Long grupoId,
        String grupoNome,
        Long experienciaId,
        String experienciaNome,
        Long estacaoId,
        String estacaoNome,
        String deviceId,
        Long sensorId,
        String sensorNome,
        String tipoSensor,
        BigDecimal valor,
        String unidade,
        LocalDateTime dataRegisto
) {
}
