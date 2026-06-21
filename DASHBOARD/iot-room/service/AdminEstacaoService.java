package com.iotroom.iotroom.service;

import com.iotroom.iotroom.dto.AdminEstacaoDTO;
import com.iotroom.iotroom.dto.AdminEstacaoForm;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class AdminEstacaoService {

    private final JdbcTemplate jdbcTemplate;

    public AdminEstacaoService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<AdminEstacaoDTO> listar(String termo, String estado) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    e.id,
                    e.nome,
                    e.device_id,
                    e.localizacao,
                    e.ativa,
                    e.criada_em,
                    COUNT(DISTINCT s.id) AS total_sensores,
                    MAX(l.data_registo) AS ultima_leitura_em
                FROM estacoes e
                LEFT JOIN sensores s
                    ON s.estacao_id = e.id
                LEFT JOIN leituras_sensor l
                    ON l.sensor_id = s.id
                WHERE 1 = 1
                """);

        List<Object> params = new ArrayList<>();

        if (termo != null && !termo.isBlank()) {
            sql.append("""
                    AND (
                        LOWER(e.nome) LIKE ?
                        OR LOWER(e.device_id) LIKE ?
                        OR LOWER(e.localizacao) LIKE ?
                    )
                    """);

            String like = "%" + termo.trim().toLowerCase() + "%";
            params.add(like);
            params.add(like);
            params.add(like);
        }

        if ("ativa".equalsIgnoreCase(estado)) {
            sql.append(" AND e.ativa = TRUE ");
        } else if ("inativa".equalsIgnoreCase(estado)) {
            sql.append(" AND e.ativa = FALSE ");
        }

        sql.append("""
                GROUP BY
                    e.id,
                    e.nome,
                    e.device_id,
                    e.localizacao,
                    e.ativa,
                    e.criada_em
                ORDER BY
                    e.ativa DESC,
                    e.nome ASC
                """);

        return jdbcTemplate.query(sql.toString(), (rs, rowNum) -> new AdminEstacaoDTO(
                rs.getLong("id"),
                rs.getString("nome"),
                rs.getString("device_id"),
                rs.getString("localizacao"),
                rs.getBoolean("ativa"),
                rs.getLong("total_sensores"),
                toLocalDateTime(rs.getTimestamp("ultima_leitura_em")),
                toLocalDateTime(rs.getTimestamp("criada_em"))
        ), params.toArray());
    }

    public AdminEstacaoDTO obterPorId(Long id) {
        return jdbcTemplate.query("""
                        SELECT
                            e.id,
                            e.nome,
                            e.device_id,
                            e.localizacao,
                            e.ativa,
                            e.criada_em,
                            COUNT(DISTINCT s.id) AS total_sensores,
                            MAX(l.data_registo) AS ultima_leitura_em
                        FROM estacoes e
                        LEFT JOIN sensores s
                            ON s.estacao_id = e.id
                        LEFT JOIN leituras_sensor l
                            ON l.sensor_id = s.id
                        WHERE e.id = ?
                        GROUP BY
                            e.id,
                            e.nome,
                            e.device_id,
                            e.localizacao,
                            e.ativa,
                            e.criada_em
                        """,
                        (rs, rowNum) -> new AdminEstacaoDTO(
                                rs.getLong("id"),
                                rs.getString("nome"),
                                rs.getString("device_id"),
                                rs.getString("localizacao"),
                                rs.getBoolean("ativa"),
                                rs.getLong("total_sensores"),
                                toLocalDateTime(rs.getTimestamp("ultima_leitura_em")),
                                toLocalDateTime(rs.getTimestamp("criada_em"))
                        ),
                        id
                )
                .stream()
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Estação não encontrada."));
    }

    public void criar(AdminEstacaoForm form) {
        validarForm(form, null);

        jdbcTemplate.update("""
                INSERT INTO estacoes (
                    nome,
                    device_id,
                    localizacao,
                    ativa,
                    criada_em
                )
                VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP)
                """,
                form.getNome(),
                form.getDeviceId(),
                form.getLocalizacao(),
                form.isAtiva()
        );
    }

    public void atualizar(Long id, AdminEstacaoForm form) {
        validarForm(form, id);

        jdbcTemplate.update("""
                UPDATE estacoes
                SET nome = ?,
                    device_id = ?,
                    localizacao = ?,
                    ativa = ?
                WHERE id = ?
                """,
                form.getNome(),
                form.getDeviceId(),
                form.getLocalizacao(),
                form.isAtiva(),
                id
        );
    }

    public void alternarEstado(Long id) {
        jdbcTemplate.update("""
                UPDATE estacoes
                SET ativa = NOT ativa
                WHERE id = ?
                """, id);
    }

    private void validarForm(AdminEstacaoForm form, Long idAtual) {
        if (form.getNome() == null || form.getNome().isBlank()) {
            throw new IllegalArgumentException("O nome da estação é obrigatório.");
        }

        if (form.getDeviceId() == null || form.getDeviceId().isBlank()) {
            throw new IllegalArgumentException("O device ID é obrigatório.");
        }

        if (deviceIdJaExiste(form.getDeviceId(), idAtual)) {
            throw new IllegalArgumentException("Já existe uma estação com esse device ID.");
        }
    }

    private boolean deviceIdJaExiste(String deviceId, Long idAtual) {
        Long total;

        if (idAtual == null) {
            total = jdbcTemplate.queryForObject("""
                    SELECT COUNT(*)
                    FROM estacoes
                    WHERE LOWER(device_id) = LOWER(?)
                    """, Long.class, deviceId);
        } else {
            total = jdbcTemplate.queryForObject("""
                    SELECT COUNT(*)
                    FROM estacoes
                    WHERE LOWER(device_id) = LOWER(?)
                    AND id <> ?
                    """, Long.class, deviceId, idAtual);
        }

        return total != null && total > 0;
    }

    private LocalDateTime toLocalDateTime(Timestamp timestamp) {
        return timestamp != null ? timestamp.toLocalDateTime() : null;
    }
}