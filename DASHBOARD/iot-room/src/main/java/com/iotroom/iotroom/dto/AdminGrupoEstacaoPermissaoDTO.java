package com.iotroom.iotroom.dto;

public record AdminGrupoEstacaoPermissaoDTO(
        Long id,
        String nome,
        String deviceId,
        String localizacao,
        boolean ativa,
        boolean permitida,
        long totalSensores
) {
}