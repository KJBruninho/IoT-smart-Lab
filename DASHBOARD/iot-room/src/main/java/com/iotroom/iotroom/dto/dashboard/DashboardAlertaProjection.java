package com.iotroom.iotroom.dto.dashboard;

import java.math.BigDecimal;
import java.sql.Timestamp;

public interface DashboardAlertaProjection {

    Long getId();

    String getTitulo();

    String getMensagem();

    String getSeveridade();

    String getEstado();

    String getTipoSensor();

    BigDecimal getValorLido();

    Timestamp getCriadoEm();
}