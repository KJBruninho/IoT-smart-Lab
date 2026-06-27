package com.iotroom.iotroom.dto.aluno;

import java.util.List;

public record AlunoPerfilDTO(
        Long id,
        String nome,
        String email,
        String role,
        List<AlunoOpcaoDTO> grupos,
        List<AlunoOpcaoDTO> experiencias,
        List<AlunoLeituraDTO> ultimasLeituras
) {
}
