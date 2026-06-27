package com.iotroom.iotroom.dto.aluno;

import java.util.List;

public record AlunoGraficoDTO(
        String tipoSensor,
        String unidade,
        List<AlunoSerieGraficoDTO> series
) {
}
