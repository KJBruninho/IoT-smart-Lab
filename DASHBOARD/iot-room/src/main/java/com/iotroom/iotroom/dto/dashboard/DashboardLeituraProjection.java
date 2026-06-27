package com.iotroom.iotroom.dto.dashboard;

import java.math.BigDecimal;
import java.sql.Timestamp;

public interface DashboardLeituraProjection {

    Long getLeituraId();

    String getSensorNome();

    String getTipoSensor();

    String getUnidade();

    BigDecimal getValor();

    Timestamp getDataRegisto();

    String getEstacaoNome();

    String getDeviceId();

    String getExperienciaNome();
}