package com.iotroom.iotroom.dto.admin;

import java.time.LocalDateTime;

public record AdminUtilizadorDTO(
        Long id,
        String nome,
        String email,
        String role,
        boolean ativo,
        LocalDateTime criadoEm
) {
}