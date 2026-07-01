package com.iotroom.assistant.dto;

import jakarta.validation.constraints.NotNull;

public record AssistantFeedbackRequest(
        @NotNull
        Long mensagemId,

        boolean util,

        String comentario
) {
}