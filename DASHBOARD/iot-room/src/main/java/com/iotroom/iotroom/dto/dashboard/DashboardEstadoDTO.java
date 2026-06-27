package com.iotroom.iotroom.dto.dashboard;

import java.util.List;

import com.iotroom.iotroom.dto.leitura.UltimaLeituraDTO;

public record DashboardEstadoDTO(
        boolean mqttOnline,
        String fonte,
        long leiturasPendentesCache,
        List<UltimaLeituraDTO> leituras
) {}
