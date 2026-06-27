package com.iotroom.iotroom.dto.leitura;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record UltimaLeituraDTO(
        String tipo,
        BigDecimal valor,
        String unidade,
        LocalDateTime dataRegisto
) {}
