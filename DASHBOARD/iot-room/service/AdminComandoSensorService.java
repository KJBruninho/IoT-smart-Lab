package com.iotroom.iotroom.service;

import com.iotroom.iotroom.dto.AdminComandoSensorDTO;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class AdminComandoSensorService {

    private final JdbcTemplate jdbcTemplate;

    public AdminComandoSensorService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<AdminComandoSensorDTO> listar(String estado, String tipoSensor, String deviceId, String termo) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    c.id,
                    c.sensor_id,
                    s.nome AS sensor_nome,
                    c.tipo_sensor,
                    c.device_id,
                    c.comando,
                    c.estado,
                    c.professor_id,
                    u.nome AS professor_nome,
                    c.resposta,
                    c.ultimo_erro,
                    c.tentativas_envio,
                    c.criado_em,
                    c.publicado_em,
                    c.confirmado_em
                FROM comandos_sensor c
                INNER JOIN sensores s ON s.id = c.sensor_id
                LEFT JOIN utilizadores u ON u.id = c.professor_id
                WHERE 1 = 1
                """);

        List<Object> params = new ArrayList<>();

        if (estado != null && !estado.isBlank()) {
            sql.append(" AND c.estado = ? ");
            params.add(estado.trim().toUpperCase());
        }

        if (tipoSensor != null && !tipoSensor.isBlank()) {
            sql.append(" AND c.tipo_sensor = ? ");
            params.add(tipoSensor.trim().toUpperCase());
        }

        if (deviceId != null && !deviceId.isBlank()) {
            sql.append(" AND LOWER(c.device_id) LIKE ? ");
            params.add("%" + deviceId.trim().toLowerCase() + "%");
        }

        if (termo != null && !termo.isBlank()) {
            sql.append("""
                    AND (
                        LOWER(c.comando) LIKE ?
                        OR LOWER(c.resposta) LIKE ?
                        OR LOWER(c.ultimo_erro) LIKE ?
                        OR LOWER(s.nome) LIKE ?
                    )
                    """);

            String like = "%" + termo.trim().toLowerCase() + "%";
            params.add(like);
            params.add(like);
            params.add(like);
            params.add(like);
        }

        sql.append("""
                ORDER BY
                    CASE c.estado
                        WHEN 'ENVIADO' THEN 1
                        WHEN 'ERRO' THEN 2
                        WHEN 'CONFIRMADO' THEN 3
                        ELSE 4
                    END,
                    c.criado_em DESC,
                    c.id DESC
                LIMIT 300
                """);

        return jdbcTemplate.query(sql.toString(), (rs, rowNum) -> mapRow(rs), params.toArray());
    }

    @Transactional
    public void reenviar(Long id) {
        AdminComandoSensorDTO comando = obterPorId(id);

        if (!"ERRO".equals(comando.estado())) {
            throw new IllegalStateException("Só é possível reenviar comandos em ERRO.");
        }

        jdbcTemplate.update("""
                UPDATE comandos_sensor
                SET estado = 'ENVIADO',
                    resposta = NULL,
                    publicado_em = NULL,
                    tentativas_envio = 0,
                    ultimo_erro = NULL,
                    confirmado_em = NULL
                WHERE id = ?
                """, id);
    }

    public AdminComandoSensorDTO obterPorId(Long id) {
        return jdbcTemplate.query("""
                        SELECT
                            c.id,
                            c.sensor_id,
                            s.nome AS sensor_nome,
                            c.tipo_sensor,
                            c.device_id,
                            c.comando,
                            c.estado,
                            c.professor_id,
                            u.nome AS professor_nome,
                            c.resposta,
                            c.ultimo_erro,
                            c.tentativas_envio,
                            c.criado_em,
                            c.publicado_em,
                            c.confirmado_em
                        FROM comandos_sensor c
                        INNER JOIN sensores s ON s.id = c.sensor_id
                        LEFT JOIN utilizadores u ON u.id = c.professor_id
                        WHERE c.id = ?
                        """,
                        (rs, rowNum) -> mapRow(rs),
                        id
                )
                .stream()
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Comando não encontrado."));
    }

    private AdminComandoSensorDTO mapRow(java.sql.ResultSet rs) throws java.sql.SQLException {
        return new AdminComandoSensorDTO(
                rs.getLong("id"),

                rs.getLong("sensor_id"),
                rs.getString("sensor_nome"),
                rs.getString("tipo_sensor"),

                rs.getString("device_id"),
                rs.getString("comando"),
                rs.getString("estado"),

                getNullableLong(rs, "professor_id"),
                rs.getString("professor_nome"),

                rs.getString("resposta"),
                rs.getString("ultimo_erro"),

                rs.getInt("tentativas_envio"),

                toLocalDateTime(rs.getTimestamp("criado_em")),
                toLocalDateTime(rs.getTimestamp("publicado_em")),
                toLocalDateTime(rs.getTimestamp("confirmado_em"))
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