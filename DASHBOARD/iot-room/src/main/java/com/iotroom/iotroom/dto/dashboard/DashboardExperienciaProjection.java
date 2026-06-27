package com.iotroom.iotroom.dto.dashboard;

import java.sql.Timestamp;

public interface DashboardExperienciaProjection {

    Long getExperienciaId();

    String getNome();

    String getGrupoNome();

    String getEstado();

    Timestamp getUltimaLeituraEm();

    Long getTotalLeituras();
}