package com.iotroom.iotroom.service.aluno;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
public class AlunoForumService {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public AlunoForumService(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<ForumTopicoAlunoDTO> listarTopicos(Long utilizadorId, boolean admin, boolean professor) {
        MapSqlParameterSource params = paramsUtilizador(utilizadorId);

        String sql = """
                SELECT
                    t.id,
                    t.titulo,
                    t.mensagem,
                    t.criado_por,
                    COALESCE(u.nome, u.email, 'Utilizador') AS autor_nome,
                    t.grupo_id,
                    g.nome AS grupo_nome,
                    t.experiencia_id,
                    e.nome AS experiencia_nome,
                    t.estado,
                    t.criado_em,
                    t.atualizado_em,
                    COUNT(r.id) AS total_respostas
                FROM forum_topicos t
                INNER JOIN utilizadores u ON u.id = t.criado_por
                LEFT JOIN grupos g ON g.id = t.grupo_id
                LEFT JOIN experiencias e ON e.id = t.experiencia_id
                LEFT JOIN forum_respostas r ON r.topico_id = t.id AND r.ativo = TRUE
                WHERE %s
                GROUP BY
                    t.id,
                    t.titulo,
                    t.mensagem,
                    t.criado_por,
                    u.nome,
                    u.email,
                    t.grupo_id,
                    g.nome,
                    t.experiencia_id,
                    e.nome,
                    t.estado,
                    t.criado_em,
                    t.atualizado_em
                ORDER BY t.criado_em DESC, t.id DESC
                """.formatted(filtroTopico("t", admin, professor));

        return jdbcTemplate.query(sql, params, topicoMapper());
    }

    public ForumTopicoAlunoDTO obterTopico(Long topicoId, Long utilizadorId, boolean admin, boolean professor) {
        MapSqlParameterSource params = paramsUtilizador(utilizadorId)
                .addValue("topicoId", topicoId);

        String sql = """
                SELECT
                    t.id,
                    t.titulo,
                    t.mensagem,
                    t.criado_por,
                    COALESCE(u.nome, u.email, 'Utilizador') AS autor_nome,
                    t.grupo_id,
                    g.nome AS grupo_nome,
                    t.experiencia_id,
                    e.nome AS experiencia_nome,
                    t.estado,
                    t.criado_em,
                    t.atualizado_em,
                    COUNT(r.id) AS total_respostas
                FROM forum_topicos t
                INNER JOIN utilizadores u ON u.id = t.criado_por
                LEFT JOIN grupos g ON g.id = t.grupo_id
                LEFT JOIN experiencias e ON e.id = t.experiencia_id
                LEFT JOIN forum_respostas r ON r.topico_id = t.id AND r.ativo = TRUE
                WHERE t.id = :topicoId
                  AND %s
                GROUP BY
                    t.id,
                    t.titulo,
                    t.mensagem,
                    t.criado_por,
                    u.nome,
                    u.email,
                    t.grupo_id,
                    g.nome,
                    t.experiencia_id,
                    e.nome,
                    t.estado,
                    t.criado_em,
                    t.atualizado_em
                """.formatted(filtroTopico("t", admin, professor));

        try {
            return jdbcTemplate.queryForObject(sql, params, topicoMapper());
        } catch (EmptyResultDataAccessException e) {
            throw new IllegalArgumentException("Tópico não encontrado ou sem permissão de acesso.");
        }
    }

    public List<ForumRespostaAlunoDTO> listarRespostas(Long topicoId, Long utilizadorId, boolean admin, boolean professor) {
        obterTopico(topicoId, utilizadorId, admin, professor);

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("topicoId", topicoId);

        return jdbcTemplate.query("""
                SELECT
                    r.id,
                    r.topico_id,
                    r.autor_id,
                    COALESCE(u.nome, u.email, 'Utilizador') AS autor_nome,
                    r.mensagem,
                    r.criado_em
                FROM forum_respostas r
                INNER JOIN utilizadores u ON u.id = r.autor_id
                WHERE r.topico_id = :topicoId
                  AND r.ativo = TRUE
                ORDER BY r.criado_em ASC, r.id ASC
                """, params, respostaMapper());
    }

    public List<ForumOpcaoAlunoDTO> listarGrupos(Long utilizadorId, boolean admin, boolean professor) {
        MapSqlParameterSource params = paramsUtilizador(utilizadorId);

        if (admin || professor) {
            return jdbcTemplate.query("""
                    SELECT
                        g.id,
                        g.nome,
                        COALESCE(g.descricao, '') AS detalhe
                    FROM grupos g
                    WHERE g.ativo = TRUE
                    ORDER BY g.nome ASC
                    """, params, opcaoMapper());
        }

        return jdbcTemplate.query("""
                SELECT DISTINCT
                    g.id,
                    g.nome,
                    COALESCE(g.descricao, '') AS detalhe
                FROM grupos g
                LEFT JOIN utilizador_grupos ug
                    ON ug.grupo_id = g.id
                   AND ug.utilizador_id = :utilizadorId
                LEFT JOIN utilizadores u
                    ON u.id = :utilizadorId
                   AND u.grupo_id = g.id
                WHERE g.ativo = TRUE
                  AND (ug.utilizador_id IS NOT NULL OR u.id IS NOT NULL)
                ORDER BY g.nome ASC
                """, params, opcaoMapper());
    }

    public List<ForumOpcaoAlunoDTO> listarExperiencias(Long utilizadorId, boolean admin, boolean professor) {
        MapSqlParameterSource params = paramsUtilizador(utilizadorId);

        if (admin || professor) {
            return jdbcTemplate.query("""
                    SELECT
                        e.id,
                        e.nome,
                        CONCAT(COALESCE(g.nome, 'Sem grupo'), ' · ', e.estado) AS detalhe
                    FROM experiencias e
                    LEFT JOIN grupos g ON g.id = e.grupo_id
                    ORDER BY e.criado_em DESC, e.id DESC
                    """, params, opcaoMapper());
        }

        return jdbcTemplate.query("""
                SELECT DISTINCT
                    e.id,
                    e.nome,
                    CONCAT(COALESCE(g.nome, 'Sem grupo'), ' · ', e.estado) AS detalhe
                FROM experiencias e
                INNER JOIN grupos g ON g.id = e.grupo_id
                LEFT JOIN utilizador_grupos ug
                    ON ug.grupo_id = g.id
                   AND ug.utilizador_id = :utilizadorId
                LEFT JOIN utilizadores u
                    ON u.id = :utilizadorId
                   AND u.grupo_id = g.id
                WHERE ug.utilizador_id IS NOT NULL OR u.id IS NOT NULL
                ORDER BY e.criado_em DESC, e.id DESC
                """, params, opcaoMapper());
    }

    @Transactional
    public Long criarTopico(
            Long utilizadorId,
            boolean admin,
            boolean professor,
            String titulo,
            String mensagem,
            Long grupoId,
            Long experienciaId
    ) {
        titulo = limparTexto(titulo);
        mensagem = limparTexto(mensagem);

        if (titulo == null || titulo.isBlank()) {
            throw new IllegalArgumentException("O título é obrigatório.");
        }

        if (mensagem == null || mensagem.isBlank()) {
            throw new IllegalArgumentException("A mensagem é obrigatória.");
        }

        Long grupoFinal = normalizarId(grupoId);
        Long experienciaFinal = normalizarId(experienciaId);

        if (!admin && !professor && grupoFinal == null && experienciaFinal == null) {
            throw new IllegalArgumentException("Seleciona um grupo ou uma experiência.");
        }

        if (experienciaFinal != null) {
            Long grupoDaExperiencia = obterGrupoDaExperiencia(experienciaFinal);

            if (grupoFinal == null) {
                grupoFinal = grupoDaExperiencia;
            } else if (!Objects.equals(grupoFinal, grupoDaExperiencia)) {
                throw new IllegalArgumentException("A experiência selecionada não pertence ao grupo indicado.");
            }
        }

        validarAcessoGrupo(grupoFinal, utilizadorId, admin, professor);
        validarAcessoExperiencia(experienciaFinal, utilizadorId, admin, professor);

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("titulo", titulo)
                .addValue("mensagem", mensagem)
                .addValue("criadoPor", utilizadorId)
                .addValue("grupoId", grupoFinal)
                .addValue("experienciaId", experienciaFinal);

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update("""
                INSERT INTO forum_topicos (
                    titulo,
                    mensagem,
                    criado_por,
                    grupo_id,
                    experiencia_id,
                    estado,
                    criado_em
                )
                VALUES (
                    :titulo,
                    :mensagem,
                    :criadoPor,
                    :grupoId,
                    :experienciaId,
                    'ABERTO',
                    CURRENT_TIMESTAMP
                )
                """, params, keyHolder);

        Number key = keyHolder.getKey();

        if (key == null) {
            throw new IllegalStateException("Não foi possível obter o ID do tópico criado.");
        }

        return key.longValue();
    }

    @Transactional
    public void responder(
            Long topicoId,
            Long utilizadorId,
            boolean admin,
            boolean professor,
            String mensagem
    ) {
        mensagem = limparTexto(mensagem);

        if (mensagem == null || mensagem.isBlank()) {
            throw new IllegalArgumentException("A resposta não pode estar vazia.");
        }

        ForumTopicoAlunoDTO topico = obterTopico(topicoId, utilizadorId, admin, professor);

        if ("FECHADO".equalsIgnoreCase(topico.estado())) {
            throw new IllegalStateException("Este tópico está fechado.");
        }

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("topicoId", topicoId)
                .addValue("autorId", utilizadorId)
                .addValue("mensagem", mensagem);

        jdbcTemplate.update("""
                INSERT INTO forum_respostas (
                    topico_id,
                    autor_id,
                    mensagem,
                    ativo,
                    criado_em
                )
                VALUES (
                    :topicoId,
                    :autorId,
                    :mensagem,
                    TRUE,
                    CURRENT_TIMESTAMP
                )
                """, params);

        jdbcTemplate.update("""
                UPDATE forum_topicos
                SET atualizado_em = CURRENT_TIMESTAMP
                WHERE id = :topicoId
                """, params);
    }

    private void validarAcessoGrupo(Long grupoId, Long utilizadorId, boolean admin, boolean professor) {
        if (grupoId == null || admin || professor) {
            return;
        }

        MapSqlParameterSource params = paramsUtilizador(utilizadorId)
                .addValue("grupoId", grupoId);

        Integer total = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM grupos g
                LEFT JOIN utilizador_grupos ug
                    ON ug.grupo_id = g.id
                   AND ug.utilizador_id = :utilizadorId
                LEFT JOIN utilizadores u
                    ON u.id = :utilizadorId
                   AND u.grupo_id = g.id
                WHERE g.id = :grupoId
                  AND g.ativo = TRUE
                  AND (ug.utilizador_id IS NOT NULL OR u.id IS NOT NULL)
                """, params, Integer.class);

        if (total == null || total == 0) {
            throw new IllegalArgumentException("Sem permissão para usar este grupo.");
        }
    }

    private void validarAcessoExperiencia(Long experienciaId, Long utilizadorId, boolean admin, boolean professor) {
        if (experienciaId == null || admin || professor) {
            return;
        }

        MapSqlParameterSource params = paramsUtilizador(utilizadorId)
                .addValue("experienciaId", experienciaId);

        Integer total = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM experiencias e
                INNER JOIN grupos g ON g.id = e.grupo_id
                LEFT JOIN utilizador_grupos ug
                    ON ug.grupo_id = g.id
                   AND ug.utilizador_id = :utilizadorId
                LEFT JOIN utilizadores u
                    ON u.id = :utilizadorId
                   AND u.grupo_id = g.id
                WHERE e.id = :experienciaId
                  AND (ug.utilizador_id IS NOT NULL OR u.id IS NOT NULL)
                """, params, Integer.class);

        if (total == null || total == 0) {
            throw new IllegalArgumentException("Sem permissão para usar esta experiência.");
        }
    }

    private Long obterGrupoDaExperiencia(Long experienciaId) {
        try {
            return jdbcTemplate.queryForObject(
                    """
                    SELECT grupo_id
                    FROM experiencias
                    WHERE id = :experienciaId
                    """,
                    new MapSqlParameterSource().addValue("experienciaId", experienciaId),
                    Long.class
            );
        } catch (EmptyResultDataAccessException e) {
            throw new IllegalArgumentException("Experiência não encontrada.");
        }
    }

    private String filtroTopico(String alias, boolean admin, boolean professor) {
        if (admin || professor) {
            return "1 = 1";
        }

        return """
                (
                    (%1$s.grupo_id IS NULL AND %1$s.experiencia_id IS NULL)
                    OR EXISTS (
                        SELECT 1
                        FROM utilizador_grupos ug
                        WHERE ug.grupo_id = %1$s.grupo_id
                          AND ug.utilizador_id = :utilizadorId
                    )
                    OR EXISTS (
                        SELECT 1
                        FROM utilizadores u
                        WHERE u.id = :utilizadorId
                          AND u.grupo_id = %1$s.grupo_id
                    )
                    OR EXISTS (
                        SELECT 1
                        FROM experiencias e2
                        INNER JOIN utilizador_grupos ug2 ON ug2.grupo_id = e2.grupo_id
                        WHERE e2.id = %1$s.experiencia_id
                          AND ug2.utilizador_id = :utilizadorId
                    )
                    OR EXISTS (
                        SELECT 1
                        FROM experiencias e3
                        INNER JOIN utilizadores u3 ON u3.grupo_id = e3.grupo_id
                        WHERE e3.id = %1$s.experiencia_id
                          AND u3.id = :utilizadorId
                    )
                )
                """.formatted(alias);
    }

    private MapSqlParameterSource paramsUtilizador(Long utilizadorId) {
        return new MapSqlParameterSource()
                .addValue("utilizadorId", utilizadorId);
    }

    private Long normalizarId(Long id) {
        return id != null && id > 0 ? id : null;
    }

    private String limparTexto(String texto) {
        return texto != null ? texto.trim() : null;
    }

    private LocalDateTime toLocalDateTime(Timestamp timestamp) {
        return timestamp != null ? timestamp.toLocalDateTime() : null;
    }

    private RowMapper<ForumTopicoAlunoDTO> topicoMapper() {
        return (rs, rowNum) -> new ForumTopicoAlunoDTO(
                rs.getLong("id"),
                rs.getString("titulo"),
                rs.getString("mensagem"),
                rs.getLong("criado_por"),
                rs.getString("autor_nome"),
                getNullableLong(rs, "grupo_id"),
                rs.getString("grupo_nome"),
                getNullableLong(rs, "experiencia_id"),
                rs.getString("experiencia_nome"),
                rs.getString("estado"),
                toLocalDateTime(rs.getTimestamp("criado_em")),
                toLocalDateTime(rs.getTimestamp("atualizado_em")),
                rs.getLong("total_respostas")
        );
    }

    private RowMapper<ForumRespostaAlunoDTO> respostaMapper() {
        return (rs, rowNum) -> new ForumRespostaAlunoDTO(
                rs.getLong("id"),
                rs.getLong("topico_id"),
                rs.getLong("autor_id"),
                rs.getString("autor_nome"),
                rs.getString("mensagem"),
                toLocalDateTime(rs.getTimestamp("criado_em"))
        );
    }

    private RowMapper<ForumOpcaoAlunoDTO> opcaoMapper() {
        return (rs, rowNum) -> new ForumOpcaoAlunoDTO(
                rs.getLong("id"),
                rs.getString("nome"),
                rs.getString("detalhe")
        );
    }

    private Long getNullableLong(ResultSet rs, String columnName) throws SQLException {
        long value = rs.getLong(columnName);
        return rs.wasNull() ? null : value;
    }

    public record ForumTopicoAlunoDTO(
            Long id,
            String titulo,
            String mensagem,
            Long criadoPorId,
            String autorNome,
            Long grupoId,
            String grupoNome,
            Long experienciaId,
            String experienciaNome,
            String estado,
            LocalDateTime criadoEm,
            LocalDateTime atualizadoEm,
            Long totalRespostas
    ) {
    }

    public record ForumRespostaAlunoDTO(
            Long id,
            Long topicoId,
            Long autorId,
            String autorNome,
            String mensagem,
            LocalDateTime criadoEm
    ) {
    }

    public record ForumOpcaoAlunoDTO(
            Long id,
            String nome,
            String detalhe
    ) {
    }
}
