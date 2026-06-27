package com.iotroom.iotroom.dto.admin;

import java.time.LocalDateTime;

public record AdminEstacaoDTO(
        Long id,
        String nome,
        String deviceId,
        String localizacao,
        boolean ativa,
        long totalSensores,
        LocalDateTime ultimaLeituraEm,
        LocalDateTime criadaEm
) {
}