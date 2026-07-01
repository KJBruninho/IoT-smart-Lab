package com.iotroom.assistant.dto;

public record AuthUserDTO(
        Long id,
        String nome,
        String email,
        String role
) {
}