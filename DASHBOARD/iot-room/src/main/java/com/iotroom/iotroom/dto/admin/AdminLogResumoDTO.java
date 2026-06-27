package com.iotroom.iotroom.dto.admin;

import java.time.LocalDateTime;

public record AdminLogResumoDTO(
        String tipo,
        String descricao,
        String gravidade,
        LocalDateTime criadoEm
) {
}