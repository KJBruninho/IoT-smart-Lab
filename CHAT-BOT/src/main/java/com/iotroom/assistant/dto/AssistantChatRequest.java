package com.iotroom.assistant.dto;

import jakarta.validation.constraints.NotBlank;

public record AssistantChatRequest(
        @NotBlank(message = "A mensagem não pode estar vazia.")
        String mensagem,

        String paginaAtual,
        String role,
        Long estacaoId,
        String deviceId,
        String sensorTipo
) {
}