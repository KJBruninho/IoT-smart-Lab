package com.iotroom.assistant.service;


import com.iotroom.assistant.dto.AssistantChatRequest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.Authentication;
import com.iotroom.assistant.dto.AuthUserDTO;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
public class DiagnosticService {

    private final JdbcTemplate jdbcTemplate;

    public DiagnosticService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Map<String, Object> run(AssistantChatRequest request, Authentication authentication) {
        Map<String, Object> diagnostico = new LinkedHashMap<>();

        AuthUserDTO user = obterUser(authentication);

        String email = user != null ? user.email() : null;
        Long userId = user != null ? user.id() : null;
        String role = user != null ? user.role() : null;

        diagnostico.put("email", email);
        diagnostico.put("utilizadorId", userId);
        diagnostico.put("role", role);
        diagnostico.put("roleRecebida", request.role());

        diagnostico.put("utilizadorExiste", userId != null);

        Long estacaoId = request.estacaoId();

        if (estacaoId == null && request.deviceId() != null && !request.deviceId().isBlank()) {
            estacaoId = obterEstacaoIdPorDeviceId(request.deviceId());
        }

        diagnostico.put("estacaoId", estacaoId);

        if (estacaoId != null) {
            diagnostico.put("estacaoAtiva", verificarEstacaoAtiva(estacaoId));
            diagnostico.put("experienciaAtiva", verificarExperienciaAtivaPorEstacao(estacaoId));
        }

        if (estacaoId != null && request.sensorTipo() != null && !request.sensorTipo().isBlank()) {
            diagnostico.put("sensorAtivo", verificarSensorAtivo(estacaoId, request.sensorTipo()));
        }

        diagnostico.putAll(obterUltimaLeitura(estacaoId, request.sensorTipo(), request.deviceId()));

        return diagnostico;
    }

    public Long obterUtilizadorIdPorEmail(String email) {
        if (email == null || email.isBlank()) {
            return null;
        }

        try {
            return jdbcTemplate.queryForObject("""
                    SELECT id
                    FROM utilizadores
                    WHERE email = ?
                    LIMIT 1
                    """, Long.class, email);
        } catch (Exception e) {
            return null;
        }
    }

    private Long obterEstacaoIdPorDeviceId(String deviceId) {
        try {
            return jdbcTemplate.queryForObject("""
                    SELECT id
                    FROM estacoes
                    WHERE device_id = ?
                    LIMIT 1
                    """, Long.class, deviceId);
        } catch (Exception e) {
            return null;
        }
    }

    private Boolean verificarEstacaoAtiva(Long estacaoId) {
        try {
            Integer count = jdbcTemplate.queryForObject("""
                    SELECT COUNT(*)
                    FROM estacoes
                    WHERE id = ?
                      AND ativa = 1
                    """, Integer.class, estacaoId);

            return count != null && count > 0;
        } catch (Exception e) {
            return null;
        }
    }

    private Boolean verificarSensorAtivo(Long estacaoId, String sensorTipo) {
        try {
            Integer count = jdbcTemplate.queryForObject("""
                    SELECT COUNT(*)
                    FROM sensores
                    WHERE estacao_id = ?
                      AND UPPER(tipo) = UPPER(?)
                      AND ativo = 1
                    """, Integer.class, estacaoId, sensorTipo);

            return count != null && count > 0;
        } catch (Exception e) {
            return null;
        }
    }

    private Boolean verificarExperienciaAtivaPorEstacao(Long estacaoId) {
        try {
            Integer count = jdbcTemplate.queryForObject("""
                    SELECT COUNT(*)
                    FROM experiencias e
                    INNER JOIN permissao_grupo_estacao pge
                        ON pge.grupo_id = e.grupo_id
                    WHERE pge.estacao_id = ?
                      AND pge.ativo = 1
                      AND UPPER(e.estado) = 'ATIVA'
                    """, Integer.class, estacaoId);

            return count != null && count > 0;
        } catch (Exception e) {
            return null;
        }
    }
    
    private AuthUserDTO obterUser(Authentication authentication) {
        if (authentication == null || authentication.getPrincipal() == null) {
            return null;
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof AuthUserDTO user) {
            return user;
        }

        return null;
    }

    private Map<String, Object> obterUltimaLeitura(Long estacaoId, String sensorTipo, String deviceId) {
        Map<String, Object> resultado = new LinkedHashMap<>();

        try {
            StringBuilder sql = new StringBuilder("""
                    SELECT
                        ls.valor AS valor,
                        ls.data_registo AS data_registo,
                        s.tipo AS tipo_sensor,
                        s.unidade AS unidade,
                        e.nome AS estacao,
                        e.device_id AS device_id
                    FROM leituras_sensor ls
                    INNER JOIN sensores s ON s.id = ls.sensor_id
                    INNER JOIN estacoes e ON e.id = s.estacao_id
                    WHERE 1 = 1
                    """);

            List<Object> params = new ArrayList<>();

            if (estacaoId != null) {
                sql.append(" AND e.id = ? ");
                params.add(estacaoId);
            }

            if (sensorTipo != null && !sensorTipo.isBlank()) {
                sql.append(" AND UPPER(s.tipo) = UPPER(?) ");
                params.add(sensorTipo);
            }

            if (deviceId != null && !deviceId.isBlank()) {
                sql.append(" AND e.device_id = ? ");
                params.add(deviceId);
            }

            sql.append(" ORDER BY ls.data_registo DESC LIMIT 1 ");

            List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql.toString(), params.toArray());

            if (rows.isEmpty()) {
                resultado.put("ultimaLeituraEncontrada", false);
                return resultado;
            }

            Map<String, Object> row = rows.get(0);

            resultado.put("ultimaLeituraEncontrada", true);
            resultado.put("ultimaLeituraValor", row.get("valor"));
            resultado.put("ultimaLeituraData", row.get("data_registo"));
            resultado.put("ultimaLeituraTipoSensor", row.get("tipo_sensor"));
            resultado.put("ultimaLeituraUnidade", row.get("unidade"));
            resultado.put("ultimaLeituraEstacao", row.get("estacao"));
            resultado.put("ultimaLeituraDeviceId", row.get("device_id"));

            Object data = row.get("data_registo");

            if (data instanceof Timestamp timestamp) {
                LocalDateTime dataLeitura = timestamp.toLocalDateTime();
                long minutos = ChronoUnit.MINUTES.between(dataLeitura, LocalDateTime.now());
                resultado.put("minutosDesdeUltimaLeitura", minutos);
            }

            return resultado;

        } catch (Exception e) {
            resultado.put("ultimaLeituraEncontrada", null);
            resultado.put("erroDiagnosticoUltimaLeitura", e.getMessage());
            return resultado;
        }
    }
}