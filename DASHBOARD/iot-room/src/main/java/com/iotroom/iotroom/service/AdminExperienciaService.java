package com.iotroom.iotroom.service;

import com.iotroom.iotroom.dto.AdminEstacaoOptionDTO;
import com.iotroom.iotroom.dto.AdminExperienciaDTO;
import com.iotroom.iotroom.dto.AdminExperienciaForm;
import com.iotroom.iotroom.dto.AdminGrupoOptionDTO;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class AdminExperienciaService {

    private final JdbcTemplate jdbcTemplate;

    public AdminExperienciaService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<AdminExperienciaDTO> listar(String termo, String estado, Long grupoId) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    exp.id,
                    exp.nome,
                    exp.descricao,
                    exp.estado,
                    exp.grupo_id,
                    g.nome AS grupo_nome,
                    exp.criado_por,
                    u.nome AS criado_por_nome,
                    exp.data_inicio,
                    exp.data_fim,
                    exp.criada_em,
                    exp.atualizado_em,
                    COUNT(DISTINCT ee.estacao_id) AS total_estacoes,
                    COUNT(DISTINCT s.id) AS total_sensores,
                    COUNT(DISTINCT l.id) AS total_leituras,
                    MIN(l.data_registo) AS primeira_leitura_em,
                    MAX(l.data_registo) AS ultima_leitura_em
                FROM experiencias exp
                INNER JOIN grupos g ON g.id = exp.grupo_id
                INNER JOIN utilizadores u ON u.id = exp.criado_por
                LEFT JOIN experiencia_estacoes ee ON ee.experiencia_id = exp.id
                LEFT JOIN sensores s ON s.estacao_id = ee.estacao_id
                LEFT JOIN leituras_sensor l ON l.experiencia_id = exp.id
                WHERE 1 = 1
                """);

        List<Object> params = new ArrayList<>();

        if (termo != null && !termo.isBlank()) {
            sql.append("""
                    AND (
                        LOWER(exp.nome) LIKE ?
                        OR LOWER(exp.descricao) LIKE ?
                        OR LOWER(g.nome) LIKE ?
                    )
                    """);

            String like = "%" + termo.trim().toLowerCase() + "%";
            params.add(like);
            params.add(like);
            params.add(like);
        }

        if (estado != null && !estado.isBlank()) {
            sql.append(" AND exp.estado = ? ");
            params.add(estado.trim().toUpperCase());
        }

        if (grupoId != null) {
            sql.append(" AND exp.grupo_id = ? ");
            params.add(grupoId);
        }

        sql.append("""
                GROUP BY
                    exp.id,
                    exp.nome,
                    exp.descricao,
                    exp.estado,
                    exp.grupo_id,
                    g.nome,
                    exp.criado_por,
                    u.nome,
                    exp.data_inicio,
                    exp.data_fim,
                    exp.criada_em,
                    exp.atualizado_em
                ORDER BY
                    CASE exp.estado
                        WHEN 'ATIVA' THEN 1
                        WHEN 'CRIADA' THEN 2
                        WHEN 'FINALIZADA' THEN 3
                        WHEN 'CANCELADA' THEN 4
                        ELSE 5
                    END,
                    exp.data_inicio DESC,
                    exp.id DESC
                """);

        return jdbcTemplate.query(sql.toString(), (rs, rowNum) -> mapExperiencia(rs), params.toArray());
    }

    public AdminExperienciaDTO obterPorId(Long id) {
        return jdbcTemplate.query("""
                        SELECT
                            exp.id,
                            exp.nome,
                            exp.descricao,
                            exp.estado,
                            exp.grupo_id,
                            g.nome AS grupo_nome,
                            exp.criado_por,
                            u.nome AS criado_por_nome,
                            exp.data_inicio,
                            exp.data_fim,
                            exp.criada_em,
                            exp.atualizado_em,
                            COUNT(DISTINCT ee.estacao_id) AS total_estacoes,
                            COUNT(DISTINCT s.id) AS total_sensores,
                            COUNT(DISTINCT l.id) AS total_leituras,
                            MIN(l.data_registo) AS primeira_leitura_em,
                            MAX(l.data_registo) AS ultima_leitura_em
                        FROM experiencias exp
                        INNER JOIN grupos g ON g.id = exp.grupo_id
                        INNER JOIN utilizadores u ON u.id = exp.criado_por
                        LEFT JOIN experiencia_estacoes ee ON ee.experiencia_id = exp.id
                        LEFT JOIN sensores s ON s.estacao_id = ee.estacao_id
                        LEFT JOIN leituras_sensor l ON l.experiencia_id = exp.id
                        WHERE exp.id = ?
                        GROUP BY
                            exp.id,
                            exp.nome,
                            exp.descricao,
                            exp.estado,
                            exp.grupo_id,
                            g.nome,
                            exp.criado_por,
                            u.nome,
                            exp.data_inicio,
                            exp.data_fim,
                            exp.criada_em,
                            exp.atualizado_em
                        """,
                        (rs, rowNum) -> mapExperiencia(rs),
                        id
                )
                .stream()
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Experiência não encontrada."));
    }

    public List<Long> obterEstacaoIdsDaExperiencia(Long experienciaId) {
        return jdbcTemplate.query("""
                SELECT estacao_id
                FROM experiencia_estacoes
                WHERE experiencia_id = ?
                ORDER BY ordem ASC, estacao_id ASC
                """,
                (rs, rowNum) -> rs.getLong("estacao_id"),
                experienciaId
        );
    }

    public List<AdminGrupoOptionDTO> listarGruposParaSelect() {
        return jdbcTemplate.query("""
                SELECT id, nome, ativo
                FROM grupos
                ORDER BY ativo DESC, nome ASC
                """,
                (rs, rowNum) -> new AdminGrupoOptionDTO(
                        rs.getLong("id"),
                        rs.getString("nome"),
                        rs.getBoolean("ativo")
                )
        );
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
    public void criar(AdminExperienciaForm form) {
        validarForm(form);

        Long criadoPor = obterPrimeiroAdminId();

        if (criadoPor == null) {
            throw new IllegalStateException("Não existe nenhum utilizador ADMIN para associar à criação da experiência.");
        }

        List<Long> estacaoIds = limparIds(form.getEstacaoIds());

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement("""
                    INSERT INTO experiencias (
                        nome,
                        descricao,
                        data_inicio,
                        estado,
                        grupo_id,
                        criado_por,
                        criada_em,
                        atualizado_em
                    )
                    VALUES (?, ?, ?, 'CRIADA', ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                    """, Statement.RETURN_GENERATED_KEYS);

            ps.setString(1, form.getNome());
            ps.setString(2, textoOuNull(form.getDescricao()));
            ps.setTimestamp(3, Timestamp.valueOf(form.getDataInicio()));
            ps.setLong(4, form.getGrupoId());
            ps.setLong(5, criadoPor);

            return ps;
        }, keyHolder);

        Number key = keyHolder.getKey();

        if (key == null) {
            throw new IllegalStateException("Não foi possível obter o ID da experiência criada.");
        }

        Long experienciaId = key.longValue();

        garantirPermissoesGrupoEstacao(form.getGrupoId(), estacaoIds);
        guardarEstacoesDaExperiencia(experienciaId, estacaoIds);
    }

    @Transactional
    public void atualizar(Long id, AdminExperienciaForm form) {
        AdminExperienciaDTO atual = obterPorId(id);

        if ("FINALIZADA".equals(atual.estado()) || "CANCELADA".equals(atual.estado())) {
            throw new IllegalStateException("Não é possível editar uma experiência finalizada ou cancelada.");
        }

        validarForm(form);

        List<Long> estacaoIds = limparIds(form.getEstacaoIds());

        jdbcTemplate.update("""
                UPDATE experiencias
                SET nome = ?,
                    descricao = ?,
                    data_inicio = ?,
                    grupo_id = ?,
                    atualizado_em = CURRENT_TIMESTAMP
                WHERE id = ?
                """,
                form.getNome(),
                textoOuNull(form.getDescricao()),
                Timestamp.valueOf(form.getDataInicio()),
                form.getGrupoId(),
                id
        );

        garantirPermissoesGrupoEstacao(form.getGrupoId(), estacaoIds);
        guardarEstacoesDaExperiencia(id, estacaoIds);
    }

    @Transactional
    public void iniciar(Long id) {
        validarExperienciaTemEstacoes(id);

        int updated = jdbcTemplate.update("""
                UPDATE experiencias
                SET estado = 'ATIVA',
                    atualizado_em = CURRENT_TIMESTAMP
                WHERE id = ?
                AND estado = 'CRIADA'
                """, id);

        if (updated == 0) {
            throw new IllegalStateException("Só é possível iniciar experiências no estado CRIADA.");
        }
    }

    @Transactional
    public void finalizar(Long id) {
        int updated = jdbcTemplate.update("""
                UPDATE experiencias
                SET estado = 'FINALIZADA',
                    data_fim = CURRENT_TIMESTAMP,
                    atualizado_em = CURRENT_TIMESTAMP
                WHERE id = ?
                AND estado IN ('CRIADA', 'ATIVA')
                """, id);

        if (updated == 0) {
            throw new IllegalStateException("Só é possível finalizar experiências criadas ou ativas.");
        }
    }

    @Transactional
    public void cancelar(Long id) {
        int updated = jdbcTemplate.update("""
                UPDATE experiencias
                SET estado = 'CANCELADA',
                    data_fim = CURRENT_TIMESTAMP,
                    atualizado_em = CURRENT_TIMESTAMP
                WHERE id = ?
                AND estado IN ('CRIADA', 'ATIVA')
                """, id);

        if (updated == 0) {
            throw new IllegalStateException("Só é possível cancelar experiências criadas ou ativas.");
        }
    }

    private void guardarEstacoesDaExperiencia(Long experienciaId, List<Long> estacaoIds) {
        jdbcTemplate.update("""
                DELETE FROM experiencia_estacoes
                WHERE experiencia_id = ?
                """, experienciaId);

        if (estacaoIds.isEmpty()) {
            return;
        }

        jdbcTemplate.batchUpdate("""
                INSERT INTO experiencia_estacoes (
                    experiencia_id,
                    estacao_id,
                    ordem,
                    observacao
                )
                VALUES (?, ?, ?, NULL)
                """,
                new BatchPreparedStatementSetter() {
                    @Override
                    public void setValues(PreparedStatement ps, int i) throws SQLException {
                        ps.setLong(1, experienciaId);
                        ps.setLong(2, estacaoIds.get(i));
                        ps.setInt(3, i + 1);
                    }

                    @Override
                    public int getBatchSize() {
                        return estacaoIds.size();
                    }
                }
        );
    }

    private void garantirPermissoesGrupoEstacao(Long grupoId, List<Long> estacaoIds) {
        if (estacaoIds.isEmpty()) {
            return;
        }

        jdbcTemplate.batchUpdate("""
                INSERT IGNORE INTO permissoes_grupo_estacao (
                    grupo_id,
                    estacao_id
                )
                VALUES (?, ?)
                """,
                new BatchPreparedStatementSetter() {
                    @Override
                    public void setValues(PreparedStatement ps, int i) throws SQLException {
                        ps.setLong(1, grupoId);
                        ps.setLong(2, estacaoIds.get(i));
                    }

                    @Override
                    public int getBatchSize() {
                        return estacaoIds.size();
                    }
                }
        );
    }

    private void validarForm(AdminExperienciaForm form) {
        if (form.getNome() == null || form.getNome().isBlank()) {
            throw new IllegalArgumentException("O nome da experiência é obrigatório.");
        }

        if (form.getGrupoId() == null) {
            throw new IllegalArgumentException("O grupo é obrigatório.");
        }

        if (form.getDataInicio() == null) {
            throw new IllegalArgumentException("A data de início é obrigatória.");
        }

        validarGrupoExiste(form.getGrupoId());

        List<Long> estacaoIds = limparIds(form.getEstacaoIds());

        if (estacaoIds.isEmpty()) {
            throw new IllegalArgumentException("Seleciona pelo menos uma estação.");
        }

        validarEstacoesExistem(estacaoIds);
    }

    private void validarGrupoExiste(Long grupoId) {
        Long total = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM grupos
                WHERE id = ?
                """, Long.class, grupoId);

        if (total == null || total == 0) {
            throw new IllegalArgumentException("Grupo não encontrado.");
        }
    }

    private void validarEstacoesExistem(List<Long> estacaoIds) {
        String placeholders = String.join(",", estacaoIds.stream().map(id -> "?").toList());

        Long total = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM estacoes WHERE id IN (" + placeholders + ")",
                Long.class,
                estacaoIds.toArray()
        );

        if (total == null || total != estacaoIds.size()) {
            throw new IllegalArgumentException("Uma ou mais estações selecionadas não existem.");
        }
    }

    private void validarExperienciaTemEstacoes(Long experienciaId) {
        Long total = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM experiencia_estacoes
                WHERE experiencia_id = ?
                """, Long.class, experienciaId);

        if (total == null || total == 0) {
            throw new IllegalStateException("A experiência precisa de pelo menos uma estação antes de ser iniciada.");
        }
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

    private List<Long> limparIds(List<Long> ids) {
        if (ids == null) {
            return List.of();
        }

        List<Long> resultado = new ArrayList<>();

        for (Long id : ids) {
            if (id != null && id > 0 && !resultado.contains(id)) {
                resultado.add(id);
            }
        }

        return resultado;
    }

    private AdminExperienciaDTO mapExperiencia(java.sql.ResultSet rs) throws java.sql.SQLException {
        return new AdminExperienciaDTO(
                rs.getLong("id"),
                rs.getString("nome"),
                rs.getString("descricao"),
                rs.getString("estado"),

                rs.getLong("grupo_id"),
                rs.getString("grupo_nome"),

                rs.getLong("criado_por"),
                rs.getString("criado_por_nome"),

                rs.getLong("total_estacoes"),
                rs.getLong("total_sensores"),
                rs.getLong("total_leituras"),

                toLocalDateTime(rs.getTimestamp("primeira_leitura_em")),
                toLocalDateTime(rs.getTimestamp("ultima_leitura_em")),

                toLocalDateTime(rs.getTimestamp("data_inicio")),
                toLocalDateTime(rs.getTimestamp("data_fim")),
                toLocalDateTime(rs.getTimestamp("criada_em")),
                toLocalDateTime(rs.getTimestamp("atualizado_em"))
        );
    }

    private String textoOuNull(String texto) {
        return texto != null && !texto.isBlank() ? texto.trim() : null;
    }

    private LocalDateTime toLocalDateTime(Timestamp timestamp) {
        return timestamp != null ? timestamp.toLocalDateTime() : null;
    }
}