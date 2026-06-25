package com.iotroom.iotroom.dto;

import java.sql.Timestamp;

public interface DashboardEstacaoProjection {

    Long getEstacaoId();

    String getEstacaoNome();

    String getDeviceId();

    Timestamp getUltimaLeituraEm();

    Long getTotalSensores();

    String getTiposSensores();
}