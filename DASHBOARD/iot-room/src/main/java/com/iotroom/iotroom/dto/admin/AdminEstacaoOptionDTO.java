package com.iotroom.iotroom.dto.admin;

public record AdminEstacaoOptionDTO(
        Long id,
        String nome,
        String deviceId,
        boolean ativa
) {
}