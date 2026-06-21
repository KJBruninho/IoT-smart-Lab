package com.iotroom.iotroom.dto;

import java.time.LocalDateTime;

public record AdminGrupoDTO(
        Long id,
        String nome,
        String descricao,
        boolean ativo,
        long totalAlunos,
        long totalEstacoes,
        LocalDateTime criadoEm
) {
}