package com.iotroom.iotroom.dto;

import java.math.BigDecimal;
import java.sql.Timestamp;

public interface ComparacaoLeituraProjection {

    Long getLeituraId();

    String getGrupoNome();

    String getExperienciaNome();

    String getEstacaoNome();

    String getDeviceId();

    String getSensorNome();

    String getTipoSensor();

    String getUnidade();

    BigDecimal getValor();

    Timestamp getDataRegisto();
}