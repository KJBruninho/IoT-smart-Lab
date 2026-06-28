package com.iotroom.iotroom.service.aluno;

import com.iotroom.iotroom.dto.aluno.AlunoDadosFiltroForm;
import com.iotroom.iotroom.dto.aluno.AlunoDashboardResumoDTO;
import com.iotroom.iotroom.dto.aluno.AlunoGraficoDTO;
import com.iotroom.iotroom.dto.aluno.AlunoLeituraDTO;
import com.iotroom.iotroom.dto.aluno.AlunoOpcaoDTO;
import com.iotroom.iotroom.dto.aluno.AlunoPedidoModoDTO;
import com.iotroom.iotroom.dto.aluno.AlunoPedidoModoForm;
import com.iotroom.iotroom.dto.aluno.AlunoPerfilDTO;
import com.iotroom.iotroom.dto.aluno.AlunoPontoGraficoDTO;
import com.iotroom.iotroom.dto.aluno.AlunoSerieGraficoDTO;
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
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class AlunoService {

    private static final int LIMITE_MINIMO = 1;
    private static final int LIMITE_PADRAO = 100;
    private static final int LIMITE_MAXIMO = 9999;

    private static final int LIMITE_SERIES_MINIMO = 1;
    private static final int LIMITE_SERIES_PADRAO = 4;
    private static final int LIMITE_SERIES_MAXIMO = 50;

    private static final DateTimeFormatter GRAFICO_DATETIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM HH:mm");

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public AlunoService(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public AlunoDashboardResumoDTO obterDashboard(Long alunoId) {
        return obterDashboard(alunoId, false, false);
    }

    public AlunoDashboardResumoDTO obterDashboard(Long utilizadorId, boolean admin, boolean professor) {
        long totalGrupos = count("""
                SELECT COUNT(DISTINCT g.id)
                FROM grupos g
                WHERE g.ativo = TRUE
                  AND %s
                """.formatted(sqlAcessoGrupo(admin, professor)), utilizadorId);

        long experienciasAtivas = count("""
                SELECT COUNT(DISTINCT exp.id)
                FROM experiencias exp
                INNER JOIN grupos g ON g.id = exp.grupo_id
                WHERE exp.estado IN ('CRIADA', 'ATIVA')
                  AND %s
                """.formatted(sqlAcessoExperiencia(admin, professor)), utilizadorId);

        long estacoesDisponiveis = count("""
                SELECT COUNT(DISTINCT e.id)
                FROM estacoes e
                WHERE e.ativa = TRUE
                  AND %s
                """.formatted(sqlAcessoEstacao(admin, professor)), utilizadorId);

        long leiturasVisiveis = count("""
                SELECT COUNT(DISTINCT l.id)
                FROM leituras_sensor l
                INNER JOIN experiencias exp ON exp.id = l.experiencia_id
                INNER JOIN grupos g ON g.id = exp.grupo_id
                INNER JOIN sensores s ON s.id = l.sensor_id
                INNER JOIN estacoes e ON e.id = s.estacao_id
                WHERE %s
                """.formatted(sqlAcessoLeitura(admin, professor)), utilizadorId);

        long pedidosPendentes = count("""
                SELECT COUNT(*)
                FROM pedidos_configuracao_sensor p
                WHERE p.solicitado_por = :utilizadorId
                  AND p.estado = 'PENDENTE'
                """, utilizadorId);

        AlunoLeituraDTO ultimaLeitura = listarLeituras(
                utilizadorId,
                admin,
                professor,
                new AlunoDadosFiltroForm(),
                1
        ).stream().findFirst().orElse(null);

        return new AlunoDashboardResumoDTO(
                totalGrupos,
                experienciasAtivas,
                estacoesDisponiveis,
                leiturasVisiveis,
                pedidosPendentes,
                ultimaLeitura
        );
    }

    public int normalizarLimitePublico(Integer limite) {
        return normalizarLimite(limite);
    }

    public int normalizarLimiteSeriesPublico(Integer limiteSeries) {
        return normalizarLimiteSeries(limiteSeries);
    }

    public List<AlunoLeituraDTO> listarLeituras(Long alunoId, AlunoDadosFiltroForm filtro) {
        return listarLeituras(alunoId, false, false, filtro);
    }

    public List<AlunoLeituraDTO> listarLeituras(
            Long utilizadorId,
            boolean admin,
            boolean professor,
            AlunoDadosFiltroForm filtro
    ) {
        return listarLeituras(
                utilizadorId,
                admin,
                professor,
                filtro,
                normalizarLimite(filtro != null ? filtro.getLimite() : null)
        );
    }

    private List<AlunoLeituraDTO> listarLeituras(
            Long utilizadorId,
            boolean admin,
            boolean professor,
            AlunoDadosFiltroForm filtro,
            int limite
    ) {
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
                """).append(sqlAcessoLeitura(admin, professor)).append("\n");

        MapSqlParameterSource params = paramsUtilizador(utilizadorId)
                .addValue("limite", limite);

        aplicarFiltros(sql, params, f);

        sql.append(" ORDER BY l.data_registo DESC LIMIT :limite");

        return jdbcTemplate.query(sql.toString(), params, leituraMapper());
    }

    public List<AlunoGraficoDTO> listarGraficos(
            Long utilizadorId,
            boolean admin,
            boolean professor,
            AlunoDadosFiltroForm filtro
    ) {
        return listarGraficos(
                utilizadorId,
                admin,
                professor,
                filtro,
                LIMITE_SERIES_PADRAO
        );
    }

    public List<AlunoLeituraDTO> listarLeiturasSemLimite(
            Long utilizadorId,
            boolean admin,
            boolean professor,
            AlunoDadosFiltroForm filtro
    ) {
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
                """).append(sqlAcessoLeitura(admin, professor)).append("\n");

        MapSqlParameterSource params = paramsUtilizador(utilizadorId);

        aplicarFiltros(sql, params, f);

        sql.append(" ORDER BY l.data_registo ASC");

        return jdbcTemplate.query(sql.toString(), params, leituraMapper());
    }
    
    public List<AlunoGraficoDTO> listarGraficos(
            Long utilizadorId,
            boolean admin,
            boolean professor,
            AlunoDadosFiltroForm filtro,
            Integer limiteSeries
    ) {
        AlunoDadosFiltroForm f = normalizarFiltro(filtro);

        int limiteDadosPorSerie = normalizarLimite(f.getLimite());
        int limiteSeriesPorGrafico = normalizarLimiteSeries(limiteSeries);

        StringBuilder sql = new StringBuilder("""
                SELECT
                    limited.leitura_id,
                    limited.grupo_id,
                    limited.grupo_nome,
                    limited.experiencia_id,
                    limited.experiencia_nome,
                    limited.estacao_id,
                    limited.estacao_nome,
                    limited.device_id,
                    limited.sensor_id,
                    limited.sensor_nome,
                    limited.tipo_sensor,
                    limited.valor,
                    limited.unidade,
                    limited.data_registo
                FROM (
                    SELECT
                        ranked_points.*,
                        DENSE_RANK() OVER (
                            PARTITION BY ranked_points.tipo_sensor
                            ORDER BY
                                ranked_points.ultima_serie DESC,
                                ranked_points.sensor_id ASC,
                                ranked_points.estacao_id ASC,
                                ranked_points.experiencia_id ASC
                        ) AS serie_rank
                    FROM (
                        SELECT
                            base.*,
                            ROW_NUMBER() OVER (
                                PARTITION BY
                                    base.tipo_sensor,
                                    base.sensor_id,
                                    base.estacao_id,
                                    base.experiencia_id
                                ORDER BY base.data_registo DESC
                            ) AS rn,
                            MAX(base.data_registo) OVER (
                                PARTITION BY
                                    base.tipo_sensor,
                                    base.sensor_id,
                                    base.estacao_id,
                                    base.experiencia_id
                            ) AS ultima_serie
                        FROM (
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
                            """).append(sqlAcessoLeitura(admin, professor)).append("\n");

        MapSqlParameterSource params = paramsUtilizador(utilizadorId)
                .addValue("limiteDados", limiteDadosPorSerie)
                .addValue("limiteSeries", limiteSeriesPorGrafico);

        aplicarFiltros(sql, params, f);

        sql.append("""
                        ) base
                    ) ranked_points
                ) limited
                WHERE limited.rn <= :limiteDados
                  AND limited.serie_rank <= :limiteSeries
                ORDER BY
                    limited.tipo_sensor ASC,
                    limited.serie_rank ASC,
                    limited.estacao_nome ASC,
                    limited.sensor_nome ASC,
                    limited.experiencia_nome ASC,
                    limited.data_registo ASC
                """);

        List<AlunoLeituraDTO> leiturasGrafico = jdbcTemplate.query(sql.toString(), params, leituraMapper());

        return criarGraficos(leiturasGrafico);
    }

    private void aplicarFiltros(
            StringBuilder sql,
            MapSqlParameterSource params,
            AlunoDadosFiltroForm f
    ) {
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
    }

    private List<AlunoGraficoDTO> criarGraficos(List<AlunoLeituraDTO> leituras) {
        Map<String, GraficoAcumulador> graficos = new LinkedHashMap<>();

        for (AlunoLeituraDTO leitura : leituras) {
            String tipoSensor = textoOuPadrao(leitura.tipoSensor(), "SENSOR");
            String unidade = textoOuPadrao(leitura.unidade(), "");

            GraficoAcumulador grafico = graficos.computeIfAbsent(
                    tipoSensor,
                    key -> new GraficoAcumulador(tipoSensor, unidade)
            );

            String chaveSerie = leitura.sensorId()
                    + "|"
                    + leitura.estacaoId()
                    + "|"
                    + leitura.experienciaId();

            SerieAcumulador serie = grafico.series.computeIfAbsent(
                    chaveSerie,
                    key -> new SerieAcumulador(nomeSerie(leitura), tipoSensor, unidade)
            );

            serie.pontos.add(new AlunoPontoGraficoDTO(
                    leitura.dataRegisto() != null
                            ? leitura.dataRegisto().format(GRAFICO_DATETIME_FORMATTER)
                            : "-",
                    leitura.valor()
            ));
        }

        List<AlunoGraficoDTO> resultado = new ArrayList<>();

        for (GraficoAcumulador grafico : graficos.values()) {
            List<AlunoSerieGraficoDTO> series = new ArrayList<>();

            for (SerieAcumulador serie : grafico.series.values()) {
                series.add(new AlunoSerieGraficoDTO(
                        serie.nome,
                        serie.tipoSensor,
                        serie.unidade,
                        serie.pontos
                ));
            }

            resultado.add(new AlunoGraficoDTO(
                    grafico.tipoSensor,
                    grafico.unidade,
                    series
            ));
        }

        return resultado;
    }

    private String nomeSerie(AlunoLeituraDTO leitura) {
        return textoOuPadrao(leitura.sensorNome(), "Sensor")
                + " · "
                + textoOuPadrao(leitura.estacaoNome(), "Estação");
    }

    public List<AlunoOpcaoDTO> listarGrupos(Long alunoId) {
        return listarGrupos(alunoId, false, false);
    }

    public List<AlunoOpcaoDTO> listarGrupos(Long utilizadorId, boolean admin, boolean professor) {
        return jdbcTemplate.query("""
                SELECT DISTINCT g.id,
                       g.nome,
                       COALESCE(g.descricao, '') AS detalhe
                FROM grupos g
                WHERE g.ativo = TRUE
                  AND %s
                ORDER BY g.nome ASC
                """.formatted(sqlAcessoGrupo(admin, professor)),
                paramsUtilizador(utilizadorId),
                opcaoMapper());
    }

    public List<AlunoOpcaoDTO> listarExperiencias(Long alunoId) {
        return listarExperiencias(alunoId, false, false);
    }

    public List<AlunoOpcaoDTO> listarExperiencias(Long utilizadorId, boolean admin, boolean professor) {
        return jdbcTemplate.query("""
                SELECT DISTINCT exp.id,
                       exp.nome,
                       CONCAT(g.nome, ' · ', exp.estado) AS detalhe,
                       exp.data_inicio AS ordem_data_inicio
                FROM experiencias exp
                INNER JOIN grupos g ON g.id = exp.grupo_id
                WHERE %s
                ORDER BY ordem_data_inicio DESC
                """.formatted(sqlAcessoExperiencia(admin, professor)),
                paramsUtilizador(utilizadorId),
                opcaoMapper());
    }

    public List<AlunoOpcaoDTO> listarEstacoes(Long alunoId) {
        return listarEstacoes(alunoId, false, false);
    }

    public List<AlunoOpcaoDTO> listarEstacoes(Long utilizadorId, boolean admin, boolean professor) {
        return jdbcTemplate.query("""
                SELECT DISTINCT e.id,
                       e.nome,
                       CONCAT(e.device_id, COALESCE(CONCAT(' · ', e.localizacao), '')) AS detalhe
                FROM estacoes e
                WHERE e.ativa = TRUE
                  AND %s
                ORDER BY e.nome ASC
                """.formatted(sqlAcessoEstacao(admin, professor)),
                paramsUtilizador(utilizadorId),
                opcaoMapper());
    }

    public List<AlunoOpcaoDTO> listarSensores(Long alunoId) {
        return listarSensores(alunoId, false, false);
    }

    public List<AlunoOpcaoDTO> listarSensores(Long utilizadorId, boolean admin, boolean professor) {
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
                """.formatted(sqlAcessoEstacao(admin, professor)),
                paramsUtilizador(utilizadorId),
                opcaoMapper());
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
                WHERE p.solicitado_por = :utilizadorId
                ORDER BY p.criado_em DESC
                LIMIT 30
                """, paramsUtilizador(alunoId), pedidoMapper());
    }

    @Transactional
    public void criarPedidoModo(Long alunoId, AlunoPedidoModoForm form) {
        criarPedidoModo(alunoId, false, false, form);
    }

    @Transactional
    public void criarPedidoModo(
            Long utilizadorId,
            boolean admin,
            boolean professor,
            AlunoPedidoModoForm form
    ) {
        validarPedido(form);

        Integer sensoresVisiveis = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM sensores s
                INNER JOIN estacoes e ON e.id = s.estacao_id
                WHERE s.id = :sensorId
                  AND s.ativo = TRUE
                  AND e.ativa = TRUE
                  AND %s
                """.formatted(sqlAcessoEstacao(admin, professor)),
                paramsUtilizador(utilizadorId)
                        .addValue("sensorId", form.getSensorId()),
                Integer.class);

        if (sensoresVisiveis == null || sensoresVisiveis == 0) {
            throw new IllegalArgumentException("O sensor selecionado não está disponível para este utilizador.");
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
                    :utilizadorId,
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
                paramsUtilizador(utilizadorId)
                        .addValue("sensorId", form.getSensorId())
                        .addValue("intervaloRapidoMs", form.getIntervaloRapidoMs())
                        .addValue("intervaloEstavelMs", form.getIntervaloEstavelMs())
                        .addValue("duracaoModoRapidoMs", form.getDuracaoModoRapidoMs())
                        .addValue("deltaSignificativo", form.getDeltaSignificativo())
                        .addValue("motivo", textoOuNull(form.getMotivo())));
    }

    public AlunoPerfilDTO obterPerfil(AuthenticatedUser user) {
        Long utilizadorId = user.getId();

        boolean admin = "ADMIN".equalsIgnoreCase(user.getRole());
        boolean professor = "PROFESSOR".equalsIgnoreCase(user.getRole());

        AlunoDadosFiltroForm filtro = new AlunoDadosFiltroForm();
        filtro.setLimite(8);

        return new AlunoPerfilDTO(
                user.getId(),
                user.getNome(),
                user.getEmail(),
                user.getRole(),
                listarGrupos(utilizadorId, admin, professor),
                listarExperiencias(utilizadorId, admin, professor),
                listarLeituras(utilizadorId, admin, professor, filtro)
        );
    }

    private long count(String sql, Long utilizadorId) {
        Long value = jdbcTemplate.queryForObject(sql, paramsUtilizador(utilizadorId), Long.class);
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

    private int normalizarLimiteSeries(Integer limiteSeries) {
        if (limiteSeries == null) {
            return LIMITE_SERIES_PADRAO;
        }

        if (limiteSeries < LIMITE_SERIES_MINIMO) {
            return LIMITE_SERIES_MINIMO;
        }

        return Math.min(limiteSeries, LIMITE_SERIES_MAXIMO);
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

    private String sqlAcessoGrupo(boolean admin, boolean professor) {
        if (admin) {
            return "1 = 1";
        }

        if (professor) {
            return "g.professor_id = :utilizadorId";
        }

        return """
                (
                    g.id = (SELECT u.grupo_id FROM utilizadores u WHERE u.id = :utilizadorId)
                    OR EXISTS (
                        SELECT 1 FROM utilizador_grupos ug
                        WHERE ug.grupo_id = g.id AND ug.utilizador_id = :utilizadorId
                    )
                )
                """;
    }

    private String sqlAcessoExperiencia(boolean admin, boolean professor) {
        if (admin) {
            return "1 = 1";
        }

        if (professor) {
            return """
                    (
                        g.professor_id = :utilizadorId
                        OR exp.criado_por = :utilizadorId
                    )
                    """;
        }

        return """
                (
                    g.id = (SELECT u.grupo_id FROM utilizadores u WHERE u.id = :utilizadorId)
                    OR EXISTS (
                        SELECT 1 FROM utilizador_grupos ug
                        WHERE ug.grupo_id = g.id AND ug.utilizador_id = :utilizadorId
                    )
                )
                """;
    }

    private String sqlAcessoLeitura(boolean admin, boolean professor) {
        if (admin) {
            return "1 = 1";
        }

        if (professor) {
            return """
                    (
                        g.professor_id = :utilizadorId
                        OR exp.criado_por = :utilizadorId
                    )
                    """;
        }

        return """
                (
                    g.id = (SELECT u.grupo_id FROM utilizadores u WHERE u.id = :utilizadorId)
                    OR EXISTS (
                        SELECT 1 FROM utilizador_grupos ug
                        WHERE ug.grupo_id = g.id AND ug.utilizador_id = :utilizadorId
                    )
                    OR EXISTS (
                        SELECT 1 FROM permissoes_utilizador_estacao pue
                        WHERE pue.estacao_id = e.id AND pue.utilizador_id = :utilizadorId
                    )
                )
                """;
    }

    private String sqlAcessoEstacao(boolean admin, boolean professor) {
        if (admin) {
            return "1 = 1";
        }

        if (professor) {
            return """
                    (
                        EXISTS (
                            SELECT 1
                            FROM permissoes_grupo_estacao pge
                            INNER JOIN grupos g2 ON g2.id = pge.grupo_id
                            WHERE pge.estacao_id = e.id
                              AND g2.professor_id = :utilizadorId
                        )
                        OR EXISTS (
                            SELECT 1
                            FROM experiencia_estacoes ee
                            INNER JOIN experiencias exp2 ON exp2.id = ee.experiencia_id
                            INNER JOIN grupos g2 ON g2.id = exp2.grupo_id
                            WHERE ee.estacao_id = e.id
                              AND (
                                g2.professor_id = :utilizadorId
                                OR exp2.criado_por = :utilizadorId
                              )
                        )
                    )
                    """;
        }

        return """
                (
                    EXISTS (
                        SELECT 1
                        FROM permissoes_grupo_estacao pge
                        INNER JOIN utilizador_grupos ug ON ug.grupo_id = pge.grupo_id
                        WHERE pge.estacao_id = e.id AND ug.utilizador_id = :utilizadorId
                    )
                    OR EXISTS (
                        SELECT 1
                        FROM permissoes_grupo_estacao pge
                        INNER JOIN utilizadores u ON u.grupo_id = pge.grupo_id
                        WHERE pge.estacao_id = e.id AND u.id = :utilizadorId
                    )
                    OR EXISTS (
                        SELECT 1
                        FROM permissoes_utilizador_estacao pue
                        WHERE pue.estacao_id = e.id AND pue.utilizador_id = :utilizadorId
                    )
                    OR EXISTS (
                        SELECT 1
                        FROM experiencia_estacoes ee
                        INNER JOIN experiencias exp ON exp.id = ee.experiencia_id
                        INNER JOIN grupos g ON g.id = exp.grupo_id
                        WHERE ee.estacao_id = e.id
                          AND (
                            g.id = (SELECT u.grupo_id FROM utilizadores u WHERE u.id = :utilizadorId)
                            OR EXISTS (
                                SELECT 1 FROM utilizador_grupos ug
                                WHERE ug.grupo_id = g.id AND ug.utilizador_id = :utilizadorId
                            )
                          )
                    )
                )
                """;
    }

    private MapSqlParameterSource paramsUtilizador(Long utilizadorId) {
        return new MapSqlParameterSource().addValue("utilizadorId", utilizadorId);
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

    private String textoOuPadrao(String texto, String padrao) {
        return texto != null && !texto.isBlank() ? texto.trim() : padrao;
    }

    private static class GraficoAcumulador {
        private final String tipoSensor;
        private final String unidade;
        private final Map<String, SerieAcumulador> series = new LinkedHashMap<>();

        private GraficoAcumulador(String tipoSensor, String unidade) {
            this.tipoSensor = tipoSensor;
            this.unidade = unidade;
        }
    }

    private static class SerieAcumulador {
        private final String nome;
        private final String tipoSensor;
        private final String unidade;
        private final List<AlunoPontoGraficoDTO> pontos = new ArrayList<>();

        private SerieAcumulador(String nome, String tipoSensor, String unidade) {
            this.nome = nome;
            this.tipoSensor = tipoSensor;
            this.unidade = unidade;
        }
    }
}
