package com.iotroom.iotroom.dto.leitura;

import java.math.BigDecimal;

public interface LeituraAlertaContextProjection {

    Long getLeituraId();

    Long getProfessorId();

    Long getExperienciaId();

    Long getGrupoId();

    Long getEstacaoId();

    Long getSensorId();

    String getTipoSensor();

    BigDecimal getValorLido();
}