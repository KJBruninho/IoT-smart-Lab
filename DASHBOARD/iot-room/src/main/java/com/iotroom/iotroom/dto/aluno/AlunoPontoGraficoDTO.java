package com.iotroom.iotroom.dto.aluno;

import java.math.BigDecimal;

public record AlunoPontoGraficoDTO(
        String label,
        BigDecimal valor
) {
}
