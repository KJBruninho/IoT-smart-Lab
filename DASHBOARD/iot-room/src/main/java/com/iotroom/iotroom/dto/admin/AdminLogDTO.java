package com.iotroom.iotroom.dto.admin;

import java.time.LocalDateTime;

public record AdminLogDTO(
        Long id,

        String tipoLog,
        String nivel,
        String acao,
        String mensagem,

        Long utilizadorId,
        String utilizadorEmail,

        Long estacaoId,
        String deviceId,

        Long experienciaId,
        String experiencia,

        String ip,
        String dispositivo,

        LocalDateTime criadoEm,

        String dados
) {
}