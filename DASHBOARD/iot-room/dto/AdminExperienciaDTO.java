package com.iotroom.iotroom.dto;

import java.time.LocalDateTime;

public record AdminExperienciaDTO(
        Long id,
        String nome,
        String descricao,
        String estado,

        Long grupoId,
        String grupoNome,

        Long criadoPorId,
        String criadoPorNome,

        long totalEstacoes,
        long totalSensores,
        long totalLeituras,

        LocalDateTime primeiraLeituraEm,
        LocalDateTime ultimaLeituraEm,

        LocalDateTime dataInicio,
        LocalDateTime dataFim,
        LocalDateTime criadaEm,
        LocalDateTime atualizadoEm
) {
}