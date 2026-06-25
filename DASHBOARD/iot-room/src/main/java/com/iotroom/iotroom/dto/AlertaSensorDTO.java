package com.iotroom.iotroom.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record AlertaSensorDTO(
        Long id,
        String tipoSensor,
        BigDecimal valorLido,
        BigDecimal valorMin,
        BigDecimal valorMax,
        String titulo,
        String mensagem,
        String severidade,
        String estado,
        LocalDateTime criadoEm,
        LocalDateTime lidoEm,
        LocalDateTime resolvidoEm
) {
}