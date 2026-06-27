package com.iotroom.iotroom.dto.admin;

import java.time.LocalDateTime;

public record AdminDashboardDTO(
        long totalUtilizadores,
        long totalAdmins,
        long totalProfessores,
        long totalAlunos,

        long totalGrupos,

        long totalEstacoes,
        long estacoesAtivas,
        long estacoesInativas,

        long totalSensores,
        long sensoresAtivos,
        long sensoresInativos,
        long sensoresSemDados,
        long sensoresSemComunicacao,

        long totalExperiencias,
        long experienciasAtivas,

        long pedidosPendentes,

        long leiturasHoje,
        LocalDateTime ultimaLeituraEm
) {
}