package com.iotroom.iotroom.dto;

import java.sql.Timestamp;

public interface GrupoMembroProjection {

    Long getUtilizadorId();

    String getNome();

    String getEmail();

    Long getRoleGrupoId();

    String getRoleGrupo();

    Timestamp getCriadoEm();
}