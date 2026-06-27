package com.iotroom.iotroom.dto.aluno;

import java.util.List;

public record AlunoSerieGraficoDTO(
        String nome,
        String tipoSensor,
        String unidade,
        List<AlunoPontoGraficoDTO> pontos
) {
}
