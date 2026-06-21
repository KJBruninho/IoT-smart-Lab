package com.iotroom.iotroom.service;

import com.iotroom.iotroom.dto.AdminEstacaoOptionDTO;
import com.iotroom.iotroom.dto.AdminSensorDTO;
import com.iotroom.iotroom.dto.AdminSensorForm;
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
public class AdminSensorService {

    private final JdbcTemplate jdbcTemplate;

    public AdminSensorService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
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
    
    private void criarComandoCalibracaoSeAlterou(AdminSensorDTO sensorAntes, AdminSensorDTO sensorDepois) {
        if (sensorAntes == null || sensorDepois == null) {
            return;
        }

        String tipo = sensorDepois.tipo();

        if (tipo == null) {
            return;
        }

        boolean alterouFator = sensorAntes.fatorCalibracao().compareTo(sensorDepois.fatorCalibracao()) != 0;
        boolean alterouOffset = sensorAntes.offsetCalibracao().compareTo(sensorDepois.offsetCalibracao()) != 0;

        String comando;

        if ("TDS".equals(tipo)) {
            if (!alterouFator) {
                return;
            }

            comando = "SET_CALIBRATION:TDS:FACTOR=" + sensorDepois.fatorCalibracao().toPlainString();

        } else if ("PH".equals(tipo)) {
            if (!alterouFator && !alterouOffset) {
                return;
            }

            comando = "SET_CALIBRATION:PH:FACTOR=" + sensorDepois.fatorCalibracao().toPlainString()
                    + ";OFFSET=" + sensorDepois.offsetCalibracao().toPlainString();

        } else {
            return;
        }

        Long executorId = obterPrimeiroAdminId();

        if (executorId == null) {
            return;
        }

        jdbcTemplate.update("""
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
                """,
                executorId,
                sensorDepois.id(),
                sensorDepois.deviceId(),
                sensorDepois.tipo(),
                comando
        );
    }

    public List<AdminSensorDTO> listar(Long estacaoId, String tipo, String estado) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    s.id,
                    s.nome,
                    s.tipo,
                    s.unidade,
                    s.estacao_id,
                    e.nome AS estacao_nome,
                    e.device_id,
                    s.ativo,
                    s.remoto_ativo,
                    s.fator_calibracao,
                    s.offset_calibracao,
                    c.intervalo_rapido_ms,
                    c.intervalo_estavel_ms,
                    c.duracao_modo_rapido_ms,
                    c.delta_significativo,
                    (
                        SELECT l.valor
                        FROM leituras_sensor l
                        WHERE l.sensor_id = s.id
                        ORDER BY l.data_registo DESC
                        LIMIT 1
                    ) AS ultima_leitura_valor,
                    (
                        SELECT l.data_registo
                        FROM leituras_sensor l
                        WHERE l.sensor_id = s.id
                        ORDER BY l.data_registo DESC
                        LIMIT 1
                    ) AS ultima_leitura_em,
                    s.criado_em,
                    s.atualizado_em
                FROM sensores s
                INNER JOIN estacoes e ON e.id = s.estacao_id
                LEFT JOIN configuracoes_modo_sensor c ON c.sensor_id = s.id
                WHERE 1 = 1
                """);

        List<Object> params = new ArrayList<>();

        if (estacaoId != null) {
            sql.append(" AND s.estacao_id = ? ");
            params.add(estacaoId);
        }

        if (tipo != null && !tipo.isBlank()) {
            sql.append(" AND s.tipo = ? ");
            params.add(tipo.trim().toUpperCase());
        }

        if ("ativo".equalsIgnoreCase(estado)) {
            sql.append(" AND s.ativo = TRUE ");
        } else if ("inativo".equalsIgnoreCase(estado)) {
            sql.append(" AND s.ativo = FALSE ");
        } else if ("remoto_ativo".equalsIgnoreCase(estado)) {
            sql.append(" AND s.remoto_ativo = TRUE ");
        } else if ("remoto_inativo".equalsIgnoreCase(estado)) {
            sql.append(" AND s.remoto_ativo = FALSE ");
        }

        sql.append("""
                ORDER BY
                    e.nome ASC,
                    s.tipo ASC,
                    s.nome ASC
                """);

        return jdbcTemplate.query(sql.toString(), (rs, rowNum) -> mapSensor(rs), params.toArray());
    }

    public AdminSensorDTO obterPorId(Long id) {
        return jdbcTemplate.query("""
                        SELECT
                            s.id,
                            s.nome,
                            s.tipo,
                            s.unidade,
                            s.estacao_id,
                            e.nome AS estacao_nome,
                            e.device_id,
                            s.ativo,
                            s.remoto_ativo,
                            s.fator_calibracao,
                            s.offset_calibracao,
                            c.intervalo_rapido_ms,
                            c.intervalo_estavel_ms,
                            c.duracao_modo_rapido_ms,
                            c.delta_significativo,
                            (
                                SELECT l.valor
                                FROM leituras_sensor l
                                WHERE l.sensor_id = s.id
                                ORDER BY l.data_registo DESC
                                LIMIT 1
                            ) AS ultima_leitura_valor,
                            (
                                SELECT l.data_registo
                                FROM leituras_sensor l
                                WHERE l.sensor_id = s.id
                                ORDER BY l.data_registo DESC
                                LIMIT 1
                            ) AS ultima_leitura_em,
                            s.criado_em,
                            s.atualizado_em
                        FROM sensores s
                        INNER JOIN estacoes e ON e.id = s.estacao_id
                        LEFT JOIN configuracoes_modo_sensor c ON c.sensor_id = s.id
                        WHERE s.id = ?
                        """,
                        (rs, rowNum) -> mapSensor(rs),
                        id
                )
                .stream()
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Sensor não encontrado."));
    }

    public List<AdminEstacaoOptionDTO> listarEstacoesParaSelect() {
        return jdbcTemplate.query("""
                SELECT id, nome, device_id, ativa
                FROM estacoes
                ORDER BY ativa DESC, nome ASC
                """,
                (rs, rowNum) -> new AdminEstacaoOptionDTO(
                        rs.getLong("id"),
                        rs.getString("nome"),
                        rs.getString("device_id"),
                        rs.getBoolean("ativa")
                )
        );
    }

    @Transactional
    public void criar(AdminSensorForm form) {
        aplicarUnidadePadraoSeVazia(form);
        aplicarConfigPadraoSeVazia(form);
        validarForm(form, null);

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement("""
                    INSERT INTO sensores (
                        nome,
                        tipo,
                        unidade,
                        estacao_id,
                        ativo,
                        remoto_ativo,
                        fator_calibracao,
                        offset_calibracao,
                        criado_em
                    )
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)
                    """, Statement.RETURN_GENERATED_KEYS);

            ps.setString(1, form.getNome());
            ps.setString(2, form.getTipo());
            ps.setString(3, form.getUnidade());
            ps.setLong(4, form.getEstacaoId());
            ps.setBoolean(5, form.isAtivo());
            ps.setBoolean(6, form.isRemotoAtivo());
            ps.setBigDecimal(7, form.getFatorCalibracao());
            ps.setBigDecimal(8, form.getOffsetCalibracao());

            return ps;
        }, keyHolder);

        Number key = keyHolder.getKey();

        if (key == null) {
            throw new IllegalStateException("Não foi possível obter o ID do sensor criado.");
        }

        Long sensorId = key.longValue();

        guardarConfiguracaoModo(sensorId, form, null);
    }

    @Transactional
    public void atualizar(Long id, AdminSensorForm form) {
        AdminSensorDTO sensorAntes = obterPorId(id);

        aplicarUnidadePadraoSeVazia(form);
        aplicarConfigPadraoSeVazia(form);
        validarForm(form, id);

        jdbcTemplate.update("""
                UPDATE sensores
                SET nome = ?,
                    tipo = ?,
                    unidade = ?,
                    estacao_id = ?,
                    ativo = ?,
                    remoto_ativo = ?,
                    fator_calibracao = ?,
                    offset_calibracao = ?
                WHERE id = ?
                """,
                form.getNome(),
                form.getTipo(),
                form.getUnidade(),
                form.getEstacaoId(),
                form.isAtivo(),
                form.isRemotoAtivo(),
                form.getFatorCalibracao(),
                form.getOffsetCalibracao(),
                id
        );

        guardarConfiguracaoModo(id, form, null);

        AdminSensorDTO sensorDepois = obterPorId(id);
        criarComandoCalibracaoSeAlterou(sensorAntes, sensorDepois);
    }

    public void alternarEstado(Long id) {
        validarSensorExiste(id);

        jdbcTemplate.update("""
                UPDATE sensores
                SET ativo = NOT ativo
                WHERE id = ?
                """, id);
    }

    @Transactional
    public void alternarEstadoRemoto(Long id) {
        AdminSensorDTO sensor = obterPorId(id);
        boolean novoEstado = !sensor.remotoAtivo();

        jdbcTemplate.update("""
                UPDATE sensores
                SET remoto_ativo = ?
                WHERE id = ?
                """, novoEstado, id);

        Long executorId = obterPrimeiroAdminId();

        if (executorId != null) {
            String comando = novoEstado ? "SET_REMOTE_ACTIVE:1" : "SET_REMOTE_ACTIVE:0";

            jdbcTemplate.update("""
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
                    """,
                    executorId,
                    sensor.id(),
                    sensor.deviceId(),
                    sensor.tipo(),
                    comando
            );
        }
    }

    private void guardarConfiguracaoModo(Long sensorId, AdminSensorForm form, Long atualizadoPor) {
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
                sensorId,
                form.getIntervaloRapidoMs(),
                form.getIntervaloEstavelMs(),
                form.getDuracaoModoRapidoMs(),
                form.getDeltaSignificativo(),
                atualizadoPor
        );
    }

    private void validarForm(AdminSensorForm form, Long idAtual) {
        if (form.getNome() == null || form.getNome().isBlank()) {
            throw new IllegalArgumentException("O nome do sensor é obrigatório.");
        }

        if (form.getTipo() == null || form.getTipo().isBlank()) {
            throw new IllegalArgumentException("O tipo do sensor é obrigatório.");
        }

        if (!List.of("TEMPERATURA", "TDS", "PH").contains(form.getTipo())) {
            throw new IllegalArgumentException("Tipo de sensor inválido. Usa TEMPERATURA, TDS ou PH.");
        }

        if (form.getUnidade() == null || form.getUnidade().isBlank()) {
            throw new IllegalArgumentException("A unidade do sensor é obrigatória.");
        }

        if (form.getEstacaoId() == null) {
            throw new IllegalArgumentException("A estação é obrigatória.");
        }

        validarEstacaoExiste(form.getEstacaoId());

        if (sensorTipoJaExisteNaEstacao(form.getEstacaoId(), form.getTipo(), idAtual)) {
            throw new IllegalArgumentException("Já existe um sensor desse tipo nessa estação.");
        }

        if (form.getFatorCalibracao() == null) {
            throw new IllegalArgumentException("O fator de calibração é obrigatório.");
        }

        if (form.getOffsetCalibracao() == null) {
            throw new IllegalArgumentException("O offset de calibração é obrigatório.");
        }

        if (form.getIntervaloRapidoMs() == null || form.getIntervaloRapidoMs() < 1000) {
            throw new IllegalArgumentException("O intervalo rápido deve ser no mínimo 1000 ms.");
        }

        if (form.getIntervaloEstavelMs() == null || form.getIntervaloEstavelMs() < 1000) {
            throw new IllegalArgumentException("O intervalo estável deve ser no mínimo 1000 ms.");
        }

        if (form.getDuracaoModoRapidoMs() == null || form.getDuracaoModoRapidoMs() < 1000) {
            throw new IllegalArgumentException("A duração do modo rápido deve ser no mínimo 1000 ms.");
        }

        if (form.getDeltaSignificativo() == null || form.getDeltaSignificativo().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("O delta significativo não pode ser negativo.");
        }
    }

    private void validarEstacaoExiste(Long estacaoId) {
        Long total = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM estacoes
                WHERE id = ?
                """, Long.class, estacaoId);

        if (total == null || total == 0) {
            throw new IllegalArgumentException("Estação não encontrada.");
        }
    }

    private void validarSensorExiste(Long sensorId) {
        Long total = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM sensores
                WHERE id = ?
                """, Long.class, sensorId);

        if (total == null || total == 0) {
            throw new IllegalArgumentException("Sensor não encontrado.");
        }
    }

    private boolean sensorTipoJaExisteNaEstacao(Long estacaoId, String tipo, Long idAtual) {
        Long total;

        if (idAtual == null) {
            total = jdbcTemplate.queryForObject("""
                    SELECT COUNT(*)
                    FROM sensores
                    WHERE estacao_id = ?
                    AND tipo = ?
                    """, Long.class, estacaoId, tipo);
        } else {
            total = jdbcTemplate.queryForObject("""
                    SELECT COUNT(*)
                    FROM sensores
                    WHERE estacao_id = ?
                    AND tipo = ?
                    AND id <> ?
                    """, Long.class, estacaoId, tipo, idAtual);
        }

        return total != null && total > 0;
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

    private void aplicarUnidadePadraoSeVazia(AdminSensorForm form) {
        if (form.getUnidade() != null && !form.getUnidade().isBlank()) {
            return;
        }

        if ("TEMPERATURA".equals(form.getTipo())) {
            form.setUnidade("ºC");
        } else if ("TDS".equals(form.getTipo())) {
            form.setUnidade("ppm");
        } else if ("PH".equals(form.getTipo())) {
            form.setUnidade("pH");
        }
    }

    private void aplicarConfigPadraoSeVazia(AdminSensorForm form) {
        if (form.getFatorCalibracao() == null) {
            form.setFatorCalibracao(new BigDecimal("1.000000"));
        }

        if (form.getOffsetCalibracao() == null) {
            form.setOffsetCalibracao(new BigDecimal("0.000000"));
        }

        if (form.getIntervaloRapidoMs() == null) {
            form.setIntervaloRapidoMs(1000);
        }

        if (form.getIntervaloEstavelMs() == null) {
            form.setIntervaloEstavelMs(30000);
        }

        if (form.getDuracaoModoRapidoMs() == null) {
            form.setDuracaoModoRapidoMs(120000);
        }

        if (form.getDeltaSignificativo() == null) {
            if ("TEMPERATURA".equals(form.getTipo())) {
                form.setDeltaSignificativo(new BigDecimal("0.2000"));
            } else if ("TDS".equals(form.getTipo())) {
                form.setDeltaSignificativo(new BigDecimal("5.0000"));
            } else if ("PH".equals(form.getTipo())) {
                form.setDeltaSignificativo(new BigDecimal("0.1000"));
            } else {
                form.setDeltaSignificativo(new BigDecimal("1.0000"));
            }
        }
    }

    private AdminSensorDTO mapSensor(java.sql.ResultSet rs) throws java.sql.SQLException {
        LocalDateTime ultimaLeituraEm = toLocalDateTime(rs.getTimestamp("ultima_leitura_em"));

        Long segundosDesdeUltimaLeitura = null;

        if (ultimaLeituraEm != null) {
            segundosDesdeUltimaLeitura = java.time.Duration
                    .between(ultimaLeituraEm, LocalDateTime.now())
                    .getSeconds();
        }

        boolean ativo = rs.getBoolean("ativo");
        boolean remotoAtivo = rs.getBoolean("remoto_ativo");

        String estadoOperacional = calcularEstadoOperacional(
                ativo,
                remotoAtivo,
                ultimaLeituraEm,
                segundosDesdeUltimaLeitura
        );

        return new AdminSensorDTO(
                rs.getLong("id"),
                rs.getString("nome"),
                rs.getString("tipo"),
                rs.getString("unidade"),

                rs.getLong("estacao_id"),
                rs.getString("estacao_nome"),
                rs.getString("device_id"),

                ativo,
                remotoAtivo,

                rs.getBigDecimal("fator_calibracao"),
                rs.getBigDecimal("offset_calibracao"),

                rs.getObject("intervalo_rapido_ms") != null ? rs.getInt("intervalo_rapido_ms") : null,
                rs.getObject("intervalo_estavel_ms") != null ? rs.getInt("intervalo_estavel_ms") : null,
                rs.getObject("duracao_modo_rapido_ms") != null ? rs.getInt("duracao_modo_rapido_ms") : null,
                rs.getBigDecimal("delta_significativo"),

                rs.getBigDecimal("ultima_leitura_valor"),
                ultimaLeituraEm,

                estadoOperacional,
                segundosDesdeUltimaLeitura,

                toLocalDateTime(rs.getTimestamp("criado_em")),
                toLocalDateTime(rs.getTimestamp("atualizado_em"))
        );
    }
    
    private String calcularEstadoOperacional(
            boolean ativo,
            boolean remotoAtivo,
            LocalDateTime ultimaLeituraEm,
            Long segundosDesdeUltimaLeitura
    ) {
        if (!ativo) {
            return "INATIVO_BD";
        }

        if (!remotoAtivo) {
            return "DESLIGADO_ESP32";
        }

        if (ultimaLeituraEm == null) {
            return "SEM_DADOS";
        }

        int timeout = obterTimeoutSemComunicacaoSegundos();

        if (segundosDesdeUltimaLeitura != null && segundosDesdeUltimaLeitura > timeout) {
            return "SEM_COMUNICACAO";
        }

        return "ONLINE";
    }
    
    private LocalDateTime toLocalDateTime(Timestamp timestamp) {
        return timestamp != null ? timestamp.toLocalDateTime() : null;
    }
}