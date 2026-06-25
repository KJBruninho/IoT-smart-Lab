package com.iotroom.iotroom.service;

import com.iotroom.iotroom.dto.AdminPedidoConfigSensorDTO;
import com.iotroom.iotroom.dto.AdminPedidoRespostaForm;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class AdminPedidoSensorService {

    private final JdbcTemplate jdbcTemplate;

    public AdminPedidoSensorService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<AdminPedidoConfigSensorDTO> listar(String estado, String origem, String tipo) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    p.id,

                    p.sensor_id,
                    s.nome AS sensor_nome,
                    s.tipo AS tipo_sensor,
                    s.unidade,

                    e.id AS estacao_id,
                    e.nome AS estacao_nome,
                    e.device_id,

                    p.origem,
                    p.estado,

                    p.solicitado_por,
                    us.nome AS solicitado_por_nome,

                    p.analisado_por,
                    ua.nome AS analisado_por_nome,

                    p.intervalo_rapido_ms,
                    p.intervalo_estavel_ms,
                    p.duracao_modo_rapido_ms,
                    p.delta_significativo,

                    p.motivo,
                    p.resposta_professor,

                    p.comando_id,

                    p.criado_em,
                    p.analisado_em,
                    p.aplicado_em
                FROM pedidos_configuracao_sensor p
                INNER JOIN sensores s ON s.id = p.sensor_id
                INNER JOIN estacoes e ON e.id = s.estacao_id
                LEFT JOIN utilizadores us ON us.id = p.solicitado_por
                LEFT JOIN utilizadores ua ON ua.id = p.analisado_por
                WHERE 1 = 1
                """);

        List<Object> params = new ArrayList<>();

        if (estado != null && !estado.isBlank()) {
            sql.append(" AND p.estado = ? ");
            params.add(estado.trim().toUpperCase());
        }

        if (origem != null && !origem.isBlank()) {
            sql.append(" AND p.origem = ? ");
            params.add(origem.trim().toUpperCase());
        }

        if (tipo != null && !tipo.isBlank()) {
            sql.append(" AND s.tipo = ? ");
            params.add(tipo.trim().toUpperCase());
        }

        sql.append("""
                ORDER BY
                    CASE p.estado
                        WHEN 'PENDENTE' THEN 1
                        WHEN 'APROVADO' THEN 2
                        WHEN 'APLICADO' THEN 3
                        WHEN 'ERRO' THEN 4
                        WHEN 'REJEITADO' THEN 5
                        ELSE 6
                    END,
                    p.criado_em DESC
                """);

        return jdbcTemplate.query(sql.toString(), (rs, rowNum) -> mapPedido(rs), params.toArray());
    }

    public AdminPedidoConfigSensorDTO obterPorId(Long id) {
        return jdbcTemplate.query("""
                        SELECT
                            p.id,

                            p.sensor_id,
                            s.nome AS sensor_nome,
                            s.tipo AS tipo_sensor,
                            s.unidade,

                            e.id AS estacao_id,
                            e.nome AS estacao_nome,
                            e.device_id,

                            p.origem,
                            p.estado,

                            p.solicitado_por,
                            us.nome AS solicitado_por_nome,

                            p.analisado_por,
                            ua.nome AS analisado_por_nome,

                            p.intervalo_rapido_ms,
                            p.intervalo_estavel_ms,
                            p.duracao_modo_rapido_ms,
                            p.delta_significativo,

                            p.motivo,
                            p.resposta_professor,

                            p.comando_id,

                            p.criado_em,
                            p.analisado_em,
                            p.aplicado_em
                        FROM pedidos_configuracao_sensor p
                        INNER JOIN sensores s ON s.id = p.sensor_id
                        INNER JOIN estacoes e ON e.id = s.estacao_id
                        LEFT JOIN utilizadores us ON us.id = p.solicitado_por
                        LEFT JOIN utilizadores ua ON ua.id = p.analisado_por
                        WHERE p.id = ?
                        """,
                        (rs, rowNum) -> mapPedido(rs),
                        id
                )
                .stream()
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Pedido não encontrado."));
    }

    @Transactional
    public void aprovar(Long pedidoId, AdminPedidoRespostaForm form) {
        AdminPedidoConfigSensorDTO pedido = obterPorId(pedidoId);

        if (!"PENDENTE".equals(pedido.estado())) {
            throw new IllegalStateException("Só é possível aprovar pedidos pendentes.");
        }

        Long adminId = obterPrimeiroAdminId();

        if (adminId == null) {
            throw new IllegalStateException("Não existe nenhum utilizador ADMIN para associar à aprovação.");
        }

        atualizarConfiguracaoSensor(pedido, adminId);

        Long comandoId = criarComandoSensor(pedido, adminId);

        jdbcTemplate.update("""
                UPDATE pedidos_configuracao_sensor
                SET estado = 'APROVADO',
                    analisado_por = ?,
                    resposta_professor = ?,
                    comando_id = ?,
                    analisado_em = CURRENT_TIMESTAMP
                WHERE id = ?
                """,
                adminId,
                textoOuNull(form != null ? form.getResposta() : null),
                comandoId,
                pedidoId
        );
    }

    @Transactional
    public void rejeitar(Long pedidoId, AdminPedidoRespostaForm form) {
        AdminPedidoConfigSensorDTO pedido = obterPorId(pedidoId);

        if (!"PENDENTE".equals(pedido.estado())) {
            throw new IllegalStateException("Só é possível rejeitar pedidos pendentes.");
        }

        Long adminId = obterPrimeiroAdminId();

        if (adminId == null) {
            throw new IllegalStateException("Não existe nenhum utilizador ADMIN para associar à rejeição.");
        }

        jdbcTemplate.update("""
                UPDATE pedidos_configuracao_sensor
                SET estado = 'REJEITADO',
                    analisado_por = ?,
                    resposta_professor = ?,
                    analisado_em = CURRENT_TIMESTAMP
                WHERE id = ?
                """,
                adminId,
                textoOuNull(form != null ? form.getResposta() : null),
                pedidoId
        );
    }

    private void atualizarConfiguracaoSensor(AdminPedidoConfigSensorDTO pedido, Long adminId) {
        jdbcTemplate.update("""
                INSERT INTO configuracoes_modo_sensor (
                    sensor_id,
                    intervalo_rapido_ms,
                    intervalo_estavel_ms,
                    duracao_modo_rapido_ms,
                    delta_significativo,
                    atualizado_por,
                    criada_em,
                    atualizada_em
                )
                VALUES (?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                ON DUPLICATE KEY UPDATE
                    intervalo_rapido_ms = VALUES(intervalo_rapido_ms),
                    intervalo_estavel_ms = VALUES(intervalo_estavel_ms),
                    duracao_modo_rapido_ms = VALUES(duracao_modo_rapido_ms),
                    delta_significativo = VALUES(delta_significativo),
                    atualizado_por = VALUES(atualizado_por),
                    atualizada_em = CURRENT_TIMESTAMP
                """,
                pedido.sensorId(),
                pedido.intervaloRapidoMs(),
                pedido.intervaloEstavelMs(),
                pedido.duracaoModoRapidoMs(),
                pedido.deltaSignificativo(),
                adminId
        );
    }

    private Long criarComandoSensor(AdminPedidoConfigSensorDTO pedido, Long adminId) {
        String comando = "SET_CONFIG:"
                + "FAST=" + pedido.intervaloRapidoMs()
                + ";STABLE=" + pedido.intervaloEstavelMs()
                + ";FAST_DURATION=" + pedido.duracaoModoRapidoMs()
                + ";DELTA=" + pedido.deltaSignificativo();

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement("""
                    INSERT INTO comandos_sensor (
                        professor_id,
                        sensor_id,
                        device_id,
                        tipo_sensor,
                        comando,
                        estado,
                        criado_em
                    )
                    VALUES (?, ?, ?, ?, ?, 'ENVIADO', CURRENT_TIMESTAMP)
                    """, Statement.RETURN_GENERATED_KEYS);

            ps.setLong(1, adminId);
            ps.setLong(2, pedido.sensorId());
            ps.setString(3, pedido.deviceId());
            ps.setString(4, pedido.tipoSensor());
            ps.setString(5, comando);

            return ps;
        }, keyHolder);

        Number key = keyHolder.getKey();

        if (key == null) {
            throw new IllegalStateException("Não foi possível criar o comando do sensor.");
        }

        return key.longValue();
    }

    private Long obterPrimeiroAdminId() {
        return jdbcTemplate.query("""
                        SELECT id
                        FROM utilizadores
                        WHERE role = 'ADMIN'
                        ORDER BY id ASC
                        LIMIT 1
                        """,
                        (rs, rowNum) -> rs.getLong("id")
                )
                .stream()
                .findFirst()
                .orElse(null);
    }

    private AdminPedidoConfigSensorDTO mapPedido(java.sql.ResultSet rs) throws java.sql.SQLException {
        return new AdminPedidoConfigSensorDTO(
                rs.getLong("id"),

                rs.getLong("sensor_id"),
                rs.getString("sensor_nome"),
                rs.getString("tipo_sensor"),
                rs.getString("unidade"),

                rs.getLong("estacao_id"),
                rs.getString("estacao_nome"),
                rs.getString("device_id"),

                rs.getString("origem"),
                rs.getString("estado"),

                getNullableLong(rs, "solicitado_por"),
                rs.getString("solicitado_por_nome"),

                getNullableLong(rs, "analisado_por"),
                rs.getString("analisado_por_nome"),

                rs.getInt("intervalo_rapido_ms"),
                rs.getInt("intervalo_estavel_ms"),
                rs.getInt("duracao_modo_rapido_ms"),
                rs.getBigDecimal("delta_significativo"),

                rs.getString("motivo"),
                rs.getString("resposta_professor"),

                getNullableLong(rs, "comando_id"),

                toLocalDateTime(rs.getTimestamp("criado_em")),
                toLocalDateTime(rs.getTimestamp("analisado_em")),
                toLocalDateTime(rs.getTimestamp("aplicado_em"))
        );
    }

    private Long getNullableLong(java.sql.ResultSet rs, String column) throws java.sql.SQLException {
        long value = rs.getLong(column);
        return rs.wasNull() ? null : value;
    }

    private String textoOuNull(String texto) {
        return texto != null && !texto.isBlank() ? texto.trim() : null;
    }

    private LocalDateTime toLocalDateTime(Timestamp timestamp) {
        return timestamp != null ? timestamp.toLocalDateTime() : null;
    }
}