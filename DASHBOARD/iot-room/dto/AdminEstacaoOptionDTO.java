package com.iotroom.iotroom.dto;

public record AdminEstacaoOptionDTO(
        Long id,
        String nome,
        String deviceId,
        boolean ativa
) {
}