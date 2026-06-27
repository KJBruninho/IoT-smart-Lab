package com.iotroom.iotroom.dto.aluno;

public record AlunoDashboardResumoDTO(
        long totalGrupos,
        long experienciasAtivas,
        long estacoesDisponiveis,
        long leiturasVisiveis,
        long pedidosPendentes,
        AlunoLeituraDTO ultimaLeitura
) {
}
