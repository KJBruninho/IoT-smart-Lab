package com.iotroom.iotroom.service.admin;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import com.iotroom.iotroom.dto.admin.AdminLogDTO;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class AdminLogService {

    private final JdbcTemplate jdbcTemplate;

    public AdminLogService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<AdminLogDTO> listar(
            String termo,
            String tipo,
            String nivel,
            String dataInicio,
            String dataFim
    ) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    log_id,
                    tipo_log,
                    nivel,
                    acao,
                    mensagem,
                    utilizador_id,
                    utilizador_email,
                    estacao_id,
                    device_id,
                    experiencia_id,
                    experiencia,
                    ip,
                    dispositivo,
                    criado_em,
                    dados
                FROM vw_logs_completos
                WHERE 1 = 1
                """);

        List<Object> params = new ArrayList<>();

        if (termo != null && !termo.isBlank()) {
            sql.append("""
                    AND (
                        LOWER(acao) LIKE ?
                        OR LOWER(mensagem) LIKE ?
                        OR LOWER(utilizador_email) LIKE ?
                        OR LOWER(device_id) LIKE ?
                        OR LOWER(experiencia) LIKE ?
                    )
                    """);

            String like = "%" + termo.trim().toLowerCase() + "%";
            params.add(like);
            params.add(like);
            params.add(like);
            params.add(like);
            params.add(like);
        }

        if (tipo != null && !tipo.isBlank()) {
            sql.append(" AND tipo_log = ? ");
            params.add(tipo.trim().toUpperCase());
        }

        if (nivel != null && !nivel.isBlank()) {
            sql.append(" AND nivel = ? ");
            params.add(nivel.trim().toUpperCase());
        }

        if (dataInicio != null && !dataInicio.isBlank()) {
            sql.append(" AND criado_em >= ? ");
            params.add(dataInicio.trim() + " 00:00:00");
        }

        if (dataFim != null && !dataFim.isBlank()) {
            sql.append(" AND criado_em <= ? ");
            params.add(dataFim.trim() + " 23:59:59");
        }

        sql.append("""
                ORDER BY criado_em DESC, log_id DESC
                LIMIT 300
                """);

        return jdbcTemplate.query(sql.toString(), (rs, rowNum) -> mapLog(rs), params.toArray());
    }

    public AdminLogDTO obterPorId(Long id) {
        return jdbcTemplate.query("""
                        SELECT
                            log_id,
                            tipo_log,
                            nivel,
                            acao,
                            mensagem,
                            utilizador_id,
                            utilizador_email,
                            estacao_id,
                            device_id,
                            experiencia_id,
                            experiencia,
                            ip,
                            dispositivo,
                            criado_em,
                            dados
                        FROM vw_logs_completos
                        WHERE log_id = ?
                        """,
                        (rs, rowNum) -> mapLog(rs),
                        id
                )
                .stream()
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Log não encontrado."));
    }

    public List<String> listarTiposLog() {
        return jdbcTemplate.query("""
                SELECT nome
                FROM tipos_log
                ORDER BY nome ASC
                """,
                (rs, rowNum) -> rs.getString("nome")
        );
    }

    public List<String> listarNiveis() {
        return List.of(
                "TRACE",
                "DEBUG",
                "INFO",
                "WARNING",
                "ERROR",
                "CRITICAL"
        );
    }

    private AdminLogDTO mapLog(java.sql.ResultSet rs) throws java.sql.SQLException {
        return new AdminLogDTO(
                rs.getLong("log_id"),

                rs.getString("tipo_log"),
                rs.getString("nivel"),
                rs.getString("acao"),
                rs.getString("mensagem"),

                getNullableLong(rs, "utilizador_id"),
                rs.getString("utilizador_email"),

                getNullableLong(rs, "estacao_id"),
                rs.getString("device_id"),

                getNullableLong(rs, "experiencia_id"),
                rs.getString("experiencia"),

                rs.getString("ip"),
                rs.getString("dispositivo"),

                toLocalDateTime(rs.getTimestamp("criado_em")),

                rs.getString("dados")
        );
    }

    private Long getNullableLong(java.sql.ResultSet rs, String column) throws java.sql.SQLException {
        long value = rs.getLong(column);
        return rs.wasNull() ? null : value;
    }

    private LocalDateTime toLocalDateTime(Timestamp timestamp) {
        return timestamp != null ? timestamp.toLocalDateTime() : null;
    }
}