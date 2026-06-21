package com.iotroom.iotroom.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record LeituraEntradaDTO(
        String deviceId,
        String tipoSensor,
        BigDecimal valor,
        String unidade,
        LocalDateTime dataRegisto
) {}
