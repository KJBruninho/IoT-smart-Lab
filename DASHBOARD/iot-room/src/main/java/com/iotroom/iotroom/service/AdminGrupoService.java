package com.iotroom.iotroom.service;

import com.iotroom.iotroom.dto.AdminGrupoDTO;
import com.iotroom.iotroom.dto.AdminGrupoForm;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class AdminGrupoService {

    private final JdbcTemplate jdbcTemplate;

    public AdminGrupoService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<AdminGrupoDTO> listar(String termo, String estado) {
        boolean temDescricao = colunaExiste("grupos", "descricao");
        boolean temAtivo = colunaExiste("grupos", "ativo");
        boolean temCriadoEm = colunaExiste("grupos", "criado_em");

        StringBuilder sql = new StringBuilder();

        sql.append("SELECT id, nome, ");

        if (temDescricao) {
            sql.append("descricao, ");
        } else {
            sql.append("NULL AS descricao, ");
        }

        if (temAtivo) {
            sql.append("ativo, ");
        } else {
            sql.append("TRUE AS ativo, ");
        }

        if (temCriadoEm) {
            sql.append("criado_em ");
        } else {
            sql.append("NULL AS criado_em ");
        }

        sql.append("FROM grupos WHERE 1 = 1 ");

        List<Object> params = new ArrayList<>();

        if (termo != null && !termo.isBlank()) {
            if (temDescricao) {
                sql.append("""
                        AND (
                            LOWER(nome) LIKE ?
                            OR LOWER(descricao) LIKE ?
                        )
                        """);

                String like = "%" + termo.trim().toLowerCase() + "%";
                params.add(like);
                params.add(like);
            } else {
                sql.append(" AND LOWER(nome) LIKE ? ");
                params.add("%" + termo.trim().toLowerCase() + "%");
            }
        }

        if (temAtivo) {
            if ("ativo".equalsIgnoreCase(estado)) {
                sql.append(" AND ativo = TRUE ");
            } else if ("inativo".equalsIgnoreCase(estado)) {
                sql.append(" AND ativo = FALSE ");
            }
        }

        sql.append(" ORDER BY id DESC ");

        return jdbcTemplate.query(sql.toString(), (rs, rowNum) -> {
            Long grupoId = rs.getLong("id");

            return new AdminGrupoDTO(
                    grupoId,
                    rs.getString("nome"),
                    rs.getString("descricao"),
                    rs.getBoolean("ativo"),
                    contarAlunosDoGrupo(grupoId),
                    contarEstacoesDoGrupo(grupoId),
                    toLocalDateTime(rs.getTimestamp("criado_em"))
            );
        }, params.toArray());
    }

    public AdminGrupoDTO obterPorId(Long id) {
        return listar(null, null)
                .stream()
                .filter(grupo -> grupo.id().equals(id))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Grupo não encontrado."));
    }

    public void criar(AdminGrupoForm form) {
        validarForm(form, null);

        boolean temDescricao = colunaExiste("grupos", "descricao");
        boolean temAtivo = colunaExiste("grupos", "ativo");
        boolean temCriadoEm = colunaExiste("grupos", "criado_em");

        List<String> colunas = new ArrayList<>();
        List<String> valores = new ArrayList<>();
        List<Object> params = new ArrayList<>();

        colunas.add("nome");
        valores.add("?");
        params.add(form.getNome());

        if (temDescricao) {
            colunas.add("descricao");
            valores.add("?");
            params.add(form.getDescricao());
        }

        if (temAtivo) {
            colunas.add("ativo");
            valores.add("?");
            params.add(form.isAtivo());
        }

        if (temCriadoEm) {
            colunas.add("criado_em");
            valores.add("CURRENT_TIMESTAMP");
        }

        String sql = "INSERT INTO grupos (" +
                String.join(", ", colunas) +
                ") VALUES (" +
                String.join(", ", valores) +
                ")";

        jdbcTemplate.update(sql, params.toArray());
    }

    public void atualizar(Long id, AdminGrupoForm form) {
        validarForm(form, id);

        boolean temDescricao = colunaExiste("grupos", "descricao");
        boolean temAtivo = colunaExiste("grupos", "ativo");

        List<String> sets = new ArrayList<>();
        List<Object> params = new ArrayList<>();

        sets.add("nome = ?");
        params.add(form.getNome());

        if (temDescricao) {
            sets.add("descricao = ?");
            params.add(form.getDescricao());
        }

        if (temAtivo) {
            sets.add("ativo = ?");
            params.add(form.isAtivo());
        }

        params.add(id);

        String sql = "UPDATE grupos SET " + String.join(", ", sets) + " WHERE id = ?";

        jdbcTemplate.update(sql, params.toArray());
    }

    public void alternarEstado(Long id) {
        if (!colunaExiste("grupos", "ativo")) {
            throw new IllegalStateException("A tabela grupos ainda não tem a coluna ativo.");
        }

        jdbcTemplate.update("""
                UPDATE grupos
                SET ativo = NOT ativo
                WHERE id = ?
                """, id);
    }

    private void validarForm(AdminGrupoForm form, Long idAtual) {
        if (form.getNome() == null || form.getNome().isBlank()) {
            throw new IllegalArgumentException("O nome do grupo é obrigatório.");
        }

        if (nomeJaExiste(form.getNome(), idAtual)) {
            throw new IllegalArgumentException("Já existe um grupo com esse nome.");
        }
    }

    private boolean nomeJaExiste(String nome, Long idAtual) {
        Long total;

        if (idAtual == null) {
            total = jdbcTemplate.queryForObject("""
                    SELECT COUNT(*)
                    FROM grupos
                    WHERE LOWER(nome) = LOWER(?)
                    """, Long.class, nome);
        } else {
            total = jdbcTemplate.queryForObject("""
                    SELECT COUNT(*)
                    FROM grupos
                    WHERE LOWER(nome) = LOWER(?)
                    AND id <> ?
                    """, Long.class, nome, idAtual);
        }

        return total != null && total > 0;
    }

    private long contarAlunosDoGrupo(Long grupoId) {
        try {
            return jdbcTemplate.queryForObject("""
                    SELECT COUNT(*)
                    FROM utilizadores
                    WHERE grupo_id = ?
                    AND role = 'ALUNO'
                    """, Long.class, grupoId);
        } catch (Exception e) {
            return 0L;
        }
    }

    private long contarEstacoesDoGrupo(Long grupoId) {
        try {
            return jdbcTemplate.queryForObject("""
                    SELECT COUNT(*)
                    FROM permissoes_grupo_estacao
                    WHERE grupo_id = ?
                    """, Long.class, grupoId);
        } catch (Exception e) {
            return 0L;
        }
    }

    private boolean colunaExiste(String tabela, String coluna) {
        try {
            Long total = jdbcTemplate.queryForObject("""
                    SELECT COUNT(*)
                    FROM information_schema.columns
                    WHERE table_schema = DATABASE()
                    AND table_name = ?
                    AND column_name = ?
                    """, Long.class, tabela, coluna);

            return total != null && total > 0;
        } catch (Exception e) {
            return false;
        }
    }

    private LocalDateTime toLocalDateTime(Timestamp timestamp) {
        return timestamp != null ? timestamp.toLocalDateTime() : null;
    }
}