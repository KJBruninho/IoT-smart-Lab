package com.iotroom.assistant.dto;

import java.util.List;
import java.util.Map;

public record AssistantChatResponse(
        Long conversaId,
        Long mensagemId,
        String resposta,
        List<String> passos,
        String gravidade,
        boolean sugerirContactoProfessor,
        Map<String, Object> diagnostico
) {
}