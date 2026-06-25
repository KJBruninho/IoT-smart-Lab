package com.iotroom.iotroom.dto;

public record SensorDisponivelDTO(
        Long id,
        String nome,
        String tipo,
        String estacao
) {
}