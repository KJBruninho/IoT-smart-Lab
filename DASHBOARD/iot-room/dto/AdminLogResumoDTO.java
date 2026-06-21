package com.iotroom.iotroom.dto;

import java.time.LocalDateTime;

public record AdminLogResumoDTO(
        String tipo,
        String descricao,
        String gravidade,
        LocalDateTime criadoEm
) {
}