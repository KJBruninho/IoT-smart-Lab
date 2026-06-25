package com.iotroom.iotroom.dto;

import java.math.BigDecimal;

public record PedidoConfiguracaoSensorRequest(
        Long sensorId,
        Integer intervaloRapidoMs,
        Integer intervaloEstavelMs,
        Integer duracaoModoRapidoMs,
        BigDecimal deltaSignificativo,
        String motivo
) {
}