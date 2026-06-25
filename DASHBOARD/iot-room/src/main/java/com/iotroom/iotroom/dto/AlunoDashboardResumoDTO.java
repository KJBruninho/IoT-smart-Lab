package com.iotroom.iotroom.dto;

public record AlunoDashboardResumoDTO(
        long totalGrupos,
        long experienciasAtivas,
        long estacoesDisponiveis,
        long leiturasVisiveis,
        long pedidosPendentes,
        AlunoLeituraDTO ultimaLeitura
) {
}
