package com.iotroom.iotroom.dto;

import java.util.List;

public record DashboardEstadoDTO(
        boolean mqttOnline,
        String fonte,
        long leiturasPendentesCache,
        List<UltimaLeituraDTO> leituras
) {}
