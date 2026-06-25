package com.iotroom.iotroom.service;

import com.iotroom.iotroom.dto.AlunoDadosFiltroForm;
import com.iotroom.iotroom.dto.AlunoDashboardResumoDTO;
import com.iotroom.iotroom.dto.AlunoLeituraDTO;
import com.iotroom.iotroom.dto.AlunoOpcaoDTO;
import com.iotroom.iotroom.dto.AlunoPedidoModoDTO;
import com.iotroom.iotroom.dto.AlunoPedidoModoForm;
import com.iotroom.iotroom.dto.AlunoPerfilDTO;
import com.iotroom.iotroom.security.AuthenticatedUser;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class AlunoService {

    private static final int LIMITE_MINIMO = 5;
    private static final int LIMITE_PADRAO = 50;
    private static final int LIMITE_MAXIMO = 500;

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public AlunoService(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public AlunoDashboardResumoDTO obterDashboard(Long alunoId) {
        long totalGrupos = count("""
                SELECT COUNT(DISTINCT g.id)
                FROM grupos g
                WHERE g.ativo = TRUE
                  AND (
                    g.id = (SELECT u.grupo_id FROM utilizadores u WHERE u.id = :alunoId)
                    OR EXISTS (
                        SELECT 1 FROM utilizador_grupos ug
                        WHERE ug.grupo_id = g.id AND ug.utilizador_id = :alunoId
                    )
                  )
                """, alunoId);

        long experienciasAtivas = count("""
                SELECT COUNT(DISTINCT exp.id)
                FROM experiencias exp
                INNER JOIN grupos g ON g.id = exp.grupo_id
                WHERE exp.estado IN ('CRIADA', 'ATIVA')
                  AND (
                    g.id = (SELECT u.grupo_id FROM utilizadores u WHERE u.id = :alunoId)
                    OR EXISTS (
                        SELECT 1 FROM utilizador_grupos ug
                        WHERE ug.grupo_id = g.id AND ug.utilizador_id = :alunoId
                    )
                  )
                """, alunoId);

        long estacoesDisponiveis = count("""
                SELECT COUNT(DISTINCT e.id)
                FROM estacoes e
                WHERE e.ativa = TRUE
                  AND (
                    EXISTS (
                        SELECT 1
                        FROM permissoes_grupo_estacao pge
                        INNER JOIN utilizador_grupos ug ON ug.grupo_id = pge.grupo_id
                        WHERE pge.estacao_id = e.id AND ug.utilizador_id = :alunoId
                    )
                    OR EXISTS (
                        SELECT 1
                        FROM permissoes_grupo_estacao pge
                        INNER JOIN utilizadores u ON u.grupo_id = pge.grupo_id
                        WHERE pge.estacao_id = e.id AND u.id = :alunoId
                    )
                    OR EXISTS (
                        SELECT 1
                        FROM permissoes_utilizador_estacao pue
                        WHERE pue.estacao_id = e.id AND pue.utilizador_id = :alunoId
                    )
                    OR EXISTS (
                        SELECT 1
                        FROM experiencia_estacoes ee
                        INNER JOIN experiencias exp ON exp.id = ee.experiencia_id
                        INNER JOIN grupos g ON g.id = exp.grupo_id
                        WHERE ee.estacao_id = e.id
                          AND (
                            g.id = (SELECT u.grupo_id FROM utilizadores u WHERE u.id = :alunoId)
                            OR EXISTS (
                                SELECT 1 FROM utilizador_grupos ug
                                WHERE ug.grupo_id = g.id AND ug.utilizador_id = :alunoId
                            )
                          )
                    )
                  )
                """, alunoId);

        long leiturasVisiveis = count("""
                SELECT COUNT(DISTINCT l.id)
                FROM leituras_sensor l
                INNER JOIN experiencias exp ON exp.id = l.experiencia_id
                INNER JOIN grupos g ON g.id = exp.grupo_id
                INNER JOIN sensores s ON s.id = l.sensor_id
                INNER JOIN estacoes e ON e.id = s.estacao_id
                WHERE %s
                """.formatted(sqlAcessoLeituraAluno()), alunoId);

        long pedidosPendentes = count("""
                SELECT COUNT(*)
                FROM pedidos_configuracao_sensor p
                WHERE p.solicitado_por = :alunoId
                  AND p.estado = 'PENDENTE'
                """, alunoId);

        AlunoLeituraDTO ultimaLeitura = listarLeituras(alunoId, new AlunoDadosFiltroForm(), 1)
                .stream()
                .findFirst()
                .orElse(null);

        return new AlunoDashboardResumoDTO(
                totalGrupos,
                experienciasAtivas,
                estacoesDisponiveis,
                leiturasVisiveis,
                pedidosPendentes,
                ultimaLeitura
        );
    }

    public List<AlunoLeituraDTO> listarLeituras(Long alunoId, AlunoDadosFiltroForm filtro) {
        return listarLeituras(alunoId, filtro, normalizarLimite(filtro != null ? filtro.getLimite() : null));
    }

    private List<AlunoLeituraDTO> listarLeituras(Long alunoId, AlunoDadosFiltroForm filtro, int limite) {
        AlunoDadosFiltroForm f = normalizarFiltro(filtro);

        StringBuilder sql = new StringBuilder("""
                SELECT
                    l.id AS leitura_id,
                    g.id AS grupo_id,
                    g.nome AS grupo_nome,
                    exp.id AS experiencia_id,
                    exp.nome AS experiencia_nome,
                    e.id AS estacao_id,
                    e.nome AS estacao_nome,
                    e.device_id,
                    s.id AS sensor_id,
                    s.nome AS sensor_nome,
                    s.tipo AS tipo_sensor,
                    l.valor,
                    l.unidade,
                    l.data_registo
                FROM leituras_sensor l
                INNER JOIN experiencias exp ON exp.id = l.experiencia_id
                INNER JOIN grupos g ON g.id = exp.grupo_id
                INNER JOIN sensores s ON s.id = l.sensor_id
                INNER JOIN estacoes e ON e.id = s.estacao_id
                WHERE
                """).append(sqlAcessoLeituraAluno()).append("\n");

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("alunoId", alunoId)
                .addValue("limite", limite);

        if (f.getGrupoId() != null) {
            sql.append(" AND g.id = :grupoId\n");
            params.addValue("grupoId", f.getGrupoId());
        }
        if (f.getExperienciaId() != null) {
            sql.append(" AND exp.id = :experienciaId\n");
            params.addValue("experienciaId", f.getExperienciaId());
        }
        if (f.getEstacaoId() != null) {
            sql.append(" AND e.id = :estacaoId\n");
            params.addValue("estacaoId", f.getEstacaoId());
        }
        if (f.getTipoSensor() != null && !f.getTipoSensor().isBlank()) {
            sql.append(" AND s.tipo = :tipoSensor\n");
            params.addValue("tipoSensor", f.getTipoSensor());
        }
        if (f.getDataInicio() != null) {
            sql.append(" AND l.data_registo >= :dataInicio\n");
            params.addValue("dataInicio", f.getDataInicio());
        }
        if (f.getDataFim() != null) {
            sql.append(" AND l.data_registo <= :dataFim\n");
            params.addValue("dataFim", f.getDataFim());
        }

        sql.append(" ORDER BY l.data_registo DESC LIMIT :limite");

        return jdbcTemplate.query(sql.toString(), params, leituraMapper());
    }

    public List<AlunoOpcaoDTO> listarGrupos(Long alunoId) {
        return jdbcTemplate.query("""
                SELECT DISTINCT g.id, g.nome, COALESCE(g.descricao, '') AS detalhe
                FROM grupos g
                WHERE g.ativo = TRUE
                  AND (
                    g.id = (SELECT u.grupo_id FROM utilizadores u WHERE u.id = :alunoId)
                    OR EXISTS (
                        SELECT 1 FROM utilizador_grupos ug
                        WHERE ug.grupo_id = g.id AND ug.utilizador_id = :alunoId
                    )
                  )
                ORDER BY g.nome ASC
                """, paramsAluno(alunoId), opcaoMapper());
    }

    public List<AlunoOpcaoDTO> listarExperiencias(Long alunoId) {
        return jdbcTemplate.query("""
                SELECT DISTINCT exp.id,
                       exp.nome,
                       CONCAT(g.nome, ' · ', exp.estado) AS detalhe,
                       exp.data_inicio AS ordem_data_inicio
                FROM experiencias exp
                INNER JOIN grupos g ON g.id = exp.grupo_id
                WHERE %s
                ORDER BY ordem_data_inicio DESC
                """.formatted(sqlAcessoExperienciaAluno()), paramsAluno(alunoId), opcaoMapper());
    }

    public List<AlunoOpcaoDTO> listarEstacoes(Long alunoId) {
        return jdbcTemplate.query("""
                SELECT DISTINCT e.id,
                       e.nome,
                       CONCAT(e.device_id, COALESCE(CONCAT(' · ', e.localizacao), '')) AS detalhe
                FROM estacoes e
                WHERE e.ativa = TRUE
                  AND %s
                ORDER BY e.nome ASC
                """.formatted(sqlAcessoEstacaoAluno()), paramsAluno(alunoId), opcaoMapper());
    }

    public List<AlunoOpcaoDTO> listarSensores(Long alunoId) {
        return jdbcTemplate.query("""
                SELECT DISTINCT s.id,
                       CONCAT(s.nome, ' · ', s.tipo) AS nome,
                       CONCAT(e.nome, ' · ', s.unidade) AS detalhe,
                       e.nome AS ordem_estacao_nome,
                       s.tipo AS ordem_sensor_tipo,
                       s.nome AS ordem_sensor_nome
                FROM sensores s
                INNER JOIN estacoes e ON e.id = s.estacao_id
                WHERE s.ativo = TRUE
                  AND e.ativa = TRUE
                  AND %s
                ORDER BY ordem_estacao_nome ASC, ordem_sensor_tipo ASC, ordem_sensor_nome ASC
                """.formatted(sqlAcessoEstacaoAluno()), paramsAluno(alunoId), opcaoMapper());
    }

    public List<AlunoPedidoModoDTO> listarPedidos(Long alunoId) {
        return jdbcTemplate.query("""
                SELECT
                    p.id,
                    p.sensor_id,
                    s.nome AS sensor_nome,
                    s.tipo AS tipo_sensor,
                    e.nome AS estacao_nome,
                    e.device_id,
                    p.estado,
                    p.intervalo_rapido_ms,
                    p.intervalo_estavel_ms,
                    p.duracao_modo_rapido_ms,
                    p.delta_significativo,
                    p.motivo,
                    p.resposta_professor,
                    p.criado_em,
                    p.analisado_em,
                    p.aplicado_em
                FROM pedidos_configuracao_sensor p
                INNER JOIN sensores s ON s.id = p.sensor_id
                INNER JOIN estacoes e ON e.id = s.estacao_id
                WHERE p.solicitado_por = :alunoId
                ORDER BY p.criado_em DESC
                LIMIT 30
                """, paramsAluno(alunoId), pedidoMapper());
    }

    @Transactional
    public void criarPedidoModo(Long alunoId, AlunoPedidoModoForm form) {
        validarPedido(form);

        Integer sensoresVisiveis = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM sensores s
                INNER JOIN estacoes e ON e.id = s.estacao_id
                WHERE s.id = :sensorId
                  AND s.ativo = TRUE
                  AND e.ativa = TRUE
                  AND %s
                """.formatted(sqlAcessoEstacaoAluno()),
                new MapSqlParameterSource()
                        .addValue("alunoId", alunoId)
                        .addValue("sensorId", form.getSensorId()),
                Integer.class);

        if (sensoresVisiveis == null || sensoresVisiveis == 0) {
            throw new IllegalArgumentException("O sensor selecionado não está disponível para este aluno.");
        }

        jdbcTemplate.update("""
                INSERT INTO pedidos_configuracao_sensor (
                    sensor_id,
                    solicitado_por,
                    origem,
                    estado,
                    intervalo_rapido_ms,
                    intervalo_estavel_ms,
                    duracao_modo_rapido_ms,
                    delta_significativo,
                    motivo,
                    criado_em
                ) VALUES (
                    :sensorId,
                    :alunoId,
                    'ALUNO',
                    'PENDENTE',
                    :intervaloRapidoMs,
                    :intervaloEstavelMs,
                    :duracaoModoRapidoMs,
                    :deltaSignificativo,
                    :motivo,
                    CURRENT_TIMESTAMP
                )
                """,
                new MapSqlParameterSource()
                        .addValue("sensorId", form.getSensorId())
                        .addValue("alunoId", alunoId)
                        .addValue("intervaloRapidoMs", form.getIntervaloRapidoMs())
                        .addValue("intervaloEstavelMs", form.getIntervaloEstavelMs())
                        .addValue("duracaoModoRapidoMs", form.getDuracaoModoRapidoMs())
                        .addValue("deltaSignificativo", form.getDeltaSignificativo())
                        .addValue("motivo", textoOuNull(form.getMotivo())));
    }

    public AlunoPerfilDTO obterPerfil(AuthenticatedUser user) {
        Long alunoId = user.getId();
        AlunoDadosFiltroForm filtro = new AlunoDadosFiltroForm();
        filtro.setLimite(8);
        return new AlunoPerfilDTO(
                user.getId(),
                user.getNome(),
                user.getEmail(),
                user.getRole(),
                listarGrupos(alunoId),
                listarExperiencias(alunoId),
                listarLeituras(alunoId, filtro)
        );
    }

    private long count(String sql, Long alunoId) {
        Long value = jdbcTemplate.queryForObject(sql, paramsAluno(alunoId), Long.class);
        return value != null ? value : 0L;
    }

    private AlunoDadosFiltroForm normalizarFiltro(AlunoDadosFiltroForm filtro) {
        AlunoDadosFiltroForm f = filtro != null ? filtro : new AlunoDadosFiltroForm();
        f.setTipoSensor(normalizarTipoSensor(f.getTipoSensor()));
        f.setLimite(normalizarLimite(f.getLimite()));
        return f;
    }

    private String normalizarTipoSensor(String tipoSensor) {
        if (tipoSensor == null || tipoSensor.isBlank()) {
            return null;
        }
        String tipo = tipoSensor.trim().toUpperCase();
        if (!"TEMPERATURA".equals(tipo) && !"TDS".equals(tipo) && !"PH".equals(tipo)) {
            return null;
        }
        return tipo;
    }

    private int normalizarLimite(Integer limite) {
        if (limite == null) {
            return LIMITE_PADRAO;
        }
        if (limite < LIMITE_MINIMO) {
            return LIMITE_MINIMO;
        }
        return Math.min(limite, LIMITE_MAXIMO);
    }

    private void validarPedido(AlunoPedidoModoForm form) {
        if (form == null || form.getSensorId() == null) {
            throw new IllegalArgumentException("Seleciona um sensor.");
        }
        if (form.getIntervaloRapidoMs() == null || form.getIntervaloRapidoMs() < 500) {
            throw new IllegalArgumentException("O intervalo rápido deve ter pelo menos 500 ms.");
        }
        if (form.getIntervaloEstavelMs() == null || form.getIntervaloEstavelMs() < 1000) {
            throw new IllegalArgumentException("O intervalo estável deve ter pelo menos 1000 ms.");
        }
        if (form.getIntervaloEstavelMs() < form.getIntervaloRapidoMs()) {
            throw new IllegalArgumentException("O intervalo estável não pode ser menor do que o intervalo rápido.");
        }
        if (form.getDuracaoModoRapidoMs() == null || form.getDuracaoModoRapidoMs() < form.getIntervaloRapidoMs()) {
            throw new IllegalArgumentException("A duração do modo rápido deve ser igual ou superior ao intervalo rápido.");
        }
        if (form.getDeltaSignificativo() == null || form.getDeltaSignificativo().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("O delta significativo deve ser maior do que zero.");
        }
    }

    private String sqlAcessoLeituraAluno() {
        return """
                (
                    g.id = (SELECT u.grupo_id FROM utilizadores u WHERE u.id = :alunoId)
                    OR EXISTS (
                        SELECT 1 FROM utilizador_grupos ug
                        WHERE ug.grupo_id = g.id AND ug.utilizador_id = :alunoId
                    )
                    OR EXISTS (
                        SELECT 1 FROM permissoes_utilizador_estacao pue
                        WHERE pue.estacao_id = e.id AND pue.utilizador_id = :alunoId
                    )
                )
                """;
    }

    private String sqlAcessoExperienciaAluno() {
        return """
                (
                    g.id = (SELECT u.grupo_id FROM utilizadores u WHERE u.id = :alunoId)
                    OR EXISTS (
                        SELECT 1 FROM utilizador_grupos ug
                        WHERE ug.grupo_id = g.id AND ug.utilizador_id = :alunoId
                    )
                )
                """;
    }

    private String sqlAcessoEstacaoAluno() {
        return """
                (
                    EXISTS (
                        SELECT 1
                        FROM permissoes_grupo_estacao pge
                        INNER JOIN utilizador_grupos ug ON ug.grupo_id = pge.grupo_id
                        WHERE pge.estacao_id = e.id AND ug.utilizador_id = :alunoId
                    )
                    OR EXISTS (
                        SELECT 1
                        FROM permissoes_grupo_estacao pge
                        INNER JOIN utilizadores u ON u.grupo_id = pge.grupo_id
                        WHERE pge.estacao_id = e.id AND u.id = :alunoId
                    )
                    OR EXISTS (
                        SELECT 1
                        FROM permissoes_utilizador_estacao pue
                        WHERE pue.estacao_id = e.id AND pue.utilizador_id = :alunoId
                    )
                    OR EXISTS (
                        SELECT 1
                        FROM experiencia_estacoes ee
                        INNER JOIN experiencias exp ON exp.id = ee.experiencia_id
                        INNER JOIN grupos g ON g.id = exp.grupo_id
                        WHERE ee.estacao_id = e.id
                          AND (
                            g.id = (SELECT u.grupo_id FROM utilizadores u WHERE u.id = :alunoId)
                            OR EXISTS (
                                SELECT 1 FROM utilizador_grupos ug
                                WHERE ug.grupo_id = g.id AND ug.utilizador_id = :alunoId
                            )
                          )
                    )
                )
                """;
    }

    private MapSqlParameterSource paramsAluno(Long alunoId) {
        return new MapSqlParameterSource().addValue("alunoId", alunoId);
    }

    private RowMapper<AlunoOpcaoDTO> opcaoMapper() {
        return (rs, rowNum) -> new AlunoOpcaoDTO(
                rs.getLong("id"),
                rs.getString("nome"),
                rs.getString("detalhe")
        );
    }

    private RowMapper<AlunoLeituraDTO> leituraMapper() {
        return (rs, rowNum) -> new AlunoLeituraDTO(
                rs.getLong("leitura_id"),
                rs.getLong("grupo_id"),
                rs.getString("grupo_nome"),
                rs.getLong("experiencia_id"),
                rs.getString("experiencia_nome"),
                rs.getLong("estacao_id"),
                rs.getString("estacao_nome"),
                rs.getString("device_id"),
                rs.getLong("sensor_id"),
                rs.getString("sensor_nome"),
                rs.getString("tipo_sensor"),
                rs.getBigDecimal("valor"),
                rs.getString("unidade"),
                toLocalDateTime(rs.getTimestamp("data_registo"))
        );
    }

    private RowMapper<AlunoPedidoModoDTO> pedidoMapper() {
        return new RowMapper<>() {
            @Override
            public AlunoPedidoModoDTO mapRow(ResultSet rs, int rowNum) throws SQLException {
                return new AlunoPedidoModoDTO(
                        rs.getLong("id"),
                        rs.getLong("sensor_id"),
                        rs.getString("sensor_nome"),
                        rs.getString("tipo_sensor"),
                        rs.getString("estacao_nome"),
                        rs.getString("device_id"),
                        rs.getString("estado"),
                        rs.getInt("intervalo_rapido_ms"),
                        rs.getInt("intervalo_estavel_ms"),
                        rs.getInt("duracao_modo_rapido_ms"),
                        rs.getBigDecimal("delta_significativo"),
                        rs.getString("motivo"),
                        rs.getString("resposta_professor"),
                        toLocalDateTime(rs.getTimestamp("criado_em")),
                        toLocalDateTime(rs.getTimestamp("analisado_em")),
                        toLocalDateTime(rs.getTimestamp("aplicado_em"))
                );
            }
        };
    }

    private LocalDateTime toLocalDateTime(Timestamp timestamp) {
        return timestamp != null ? timestamp.toLocalDateTime() : null;
    }

    private String textoOuNull(String texto) {
        return texto != null && !texto.isBlank() ? texto.trim() : null;
    }
}
