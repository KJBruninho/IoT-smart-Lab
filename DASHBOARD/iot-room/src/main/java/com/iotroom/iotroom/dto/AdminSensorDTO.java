package com.iotroom.iotroom.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record AdminSensorDTO(
        Long id,
        String nome,
        String tipo,
        String unidade,

        Long estacaoId,
        String estacaoNome,
        String deviceId,

        boolean ativo,
        boolean remotoAtivo,

        BigDecimal fatorCalibracao,
        BigDecimal offsetCalibracao,

        Integer intervaloRapidoMs,
        Integer intervaloEstavelMs,
        Integer duracaoModoRapidoMs,
        BigDecimal deltaSignificativo,

        BigDecimal ultimaLeituraValor,
        LocalDateTime ultimaLeituraEm,

        String estadoOperacional,
        Long segundosDesdeUltimaLeitura,

        LocalDateTime criadoEm,
        LocalDateTime atualizadoEm
) {
}