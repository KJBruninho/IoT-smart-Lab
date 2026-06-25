package com.iotroom.iotroom.service;

import com.iotroom.iotroom.dto.AdminDashboardDTO;
import com.iotroom.iotroom.dto.AdminLogResumoDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class AdminDashboardService {

    private static final Logger logger = LoggerFactory.getLogger(AdminDashboardService.class);

    private final JdbcTemplate jdbcTemplate;

    public AdminDashboardService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }
    private long contarSensoresSemDados() {
        return countOrZero("""
                SELECT COUNT(*)
                FROM sensores s
                WHERE s.ativo = TRUE
                AND s.remoto_ativo = TRUE
                AND NOT EXISTS (
                    SELECT 1
                    FROM leituras_sensor l
                    WHERE l.sensor_id = s.id
                )
                """);
    }

    private long contarSensoresSemComunicacao() {
        int timeout = obterTimeoutSemComunicacaoSegundos();

        try {
            Long total = jdbcTemplate.queryForObject("""
                    SELECT COUNT(*)
                    FROM sensores s
                    INNER JOIN (
                        SELECT sensor_id, MAX(data_registo) AS ultima_leitura
                        FROM leituras_sensor
                        GROUP BY sensor_id
                    ) ult ON ult.sensor_id = s.id
                    WHERE s.ativo = TRUE
                    AND s.remoto_ativo = TRUE
                    AND TIMESTAMPDIFF(SECOND, ult.ultima_leitura, CURRENT_TIMESTAMP) > ?
                    """, Long.class, timeout);

            return total != null ? total : 0L;
        } catch (Exception e) {
            logger.warn("Não foi possível contar sensores sem comunicação: {}", e.getMessage());
            return 0L;
        }
    }

    private int obterTimeoutSemComunicacaoSegundos() {
        try {
            String valor = jdbcTemplate.query("""
                            SELECT valor
                            FROM configuracoes_sistema
                            WHERE chave = 'timeout_sem_comunicacao_segundos'
                            """,
                            (rs, rowNum) -> rs.getString("valor")
                    )
                    .stream()
                    .findFirst()
                    .orElse("120");

            return Integer.parseInt(valor);
        } catch (Exception e) {
            return 120;
        }
    }

    public AdminDashboardDTO obterResumoDashboard() {
        return new AdminDashboardDTO(
                countOrZero("SELECT COUNT(*) FROM utilizadores"),
                countOrZero("SELECT COUNT(*) FROM utilizadores WHERE role = 'ADMIN'"),
                countOrZero("SELECT COUNT(*) FROM utilizadores WHERE role = 'PROFESSOR'"),
                countOrZero("SELECT COUNT(*) FROM utilizadores WHERE role = 'ALUNO'"),

                countOrZero("SELECT COUNT(*) FROM grupos"),

                countOrZero("SELECT COUNT(*) FROM estacoes"),
                countOrZero("SELECT COUNT(*) FROM estacoes WHERE ativa = TRUE"),
                countOrZero("SELECT COUNT(*) FROM estacoes WHERE ativa = FALSE"),

                countOrZero("SELECT COUNT(*) FROM sensores"),
                countOrZero("SELECT COUNT(*) FROM sensores WHERE ativo = TRUE"),
                countOrZero("SELECT COUNT(*) FROM sensores WHERE ativo = FALSE"),
                contarSensoresSemDados(),
                contarSensoresSemComunicacao(),

                countOrZero("SELECT COUNT(*) FROM experiencias"),
                countOrZero("SELECT COUNT(*) FROM experiencias WHERE estado = 'ATIVA'"),

                countOrZero("SELECT COUNT(*) FROM pedidos_alteracao_sensor WHERE estado = 'PENDENTE'"),

                countOrZero("SELECT COUNT(*) FROM leituras_sensor WHERE DATE(data_registo) = CURDATE()"),
                dateTimeOrNull("SELECT MAX(data_registo) FROM leituras_sensor")
        );
    }

    public List<AdminLogResumoDTO> obterLogsRecentes() {
        try {
            return jdbcTemplate.query("""
                    SELECT tipo, descricao, gravidade, criado_em
                    FROM logs_sistema
                    ORDER BY criado_em DESC
                    LIMIT 8
                    """,
                    (rs, rowNum) -> new AdminLogResumoDTO(
                            rs.getString("tipo"),
                            rs.getString("descricao"),
                            rs.getString("gravidade"),
                            toLocalDateTime(rs.getTimestamp("criado_em"))
                    )
            );
        } catch (Exception e) {
            logger.warn("Não foi possível carregar logs recentes da dashboard admin: {}", e.getMessage());
            return List.of();
        }
    }

    private long countOrZero(String sql) {
        try {
            Long result = jdbcTemplate.queryForObject(sql, Long.class);
            return result != null ? result : 0L;
        } catch (Exception e) {
            logger.warn("Query da dashboard admin falhou: {} | {}", sql, e.getMessage());
            return 0L;
        }
    }

    private LocalDateTime dateTimeOrNull(String sql) {
        try {
            Timestamp timestamp = jdbcTemplate.queryForObject(sql, Timestamp.class);
            return toLocalDateTime(timestamp);
        } catch (Exception e) {
            logger.warn("Query de data da dashboard admin falhou: {} | {}", sql, e.getMessage());
            return null;
        }
    }

    private LocalDateTime toLocalDateTime(Timestamp timestamp) {
        return timestamp != null ? timestamp.toLocalDateTime() : null;
    }
}