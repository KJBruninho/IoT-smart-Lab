package com.iotroom.iotroom.service.admin;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import com.iotroom.iotroom.dto.admin.AdminUtilizadorDTO;
import com.iotroom.iotroom.dto.admin.AdminUtilizadorForm;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class AdminUtilizadorService {

    private final JdbcTemplate jdbcTemplate;

    public AdminUtilizadorService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<AdminUtilizadorDTO> listar(String termo, String role, String estado) {
        StringBuilder sql = new StringBuilder("""
                SELECT id, nome, email, role, ativo, criado_em
                FROM utilizadores
                WHERE 1 = 1
                """);

        List<Object> params = new ArrayList<>();

        if (termo != null && !termo.isBlank()) {
            sql.append("""
                    AND (
                        LOWER(nome) LIKE ?
                        OR LOWER(email) LIKE ?
                    )
                    """);

            String like = "%" + termo.trim().toLowerCase() + "%";
            params.add(like);
            params.add(like);
        }

        if (role != null && !role.isBlank()) {
            sql.append(" AND role = ? ");
            params.add(role.trim().toUpperCase());
        }

        if ("ativo".equalsIgnoreCase(estado)) {
            sql.append(" AND ativo = TRUE ");
        } else if ("inativo".equalsIgnoreCase(estado)) {
            sql.append(" AND ativo = FALSE ");
        }

        sql.append(" ORDER BY criado_em DESC, id DESC ");

        return jdbcTemplate.query(sql.toString(), this::mapRow, params.toArray());
    }

    public AdminUtilizadorDTO obterPorId(Long id) {
        return jdbcTemplate.query("""
                        SELECT id, nome, email, role, ativo, criado_em
                        FROM utilizadores
                        WHERE id = ?
                        """,
                        this::mapRow,
                        id
                )
                .stream()
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Utilizador não encontrado."));
    }

    public void criar(AdminUtilizadorForm form) {
        validarForm(form, null);

        jdbcTemplate.update("""
                INSERT INTO utilizadores (nome, email, role, ativo, criado_em)
                VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP)
                """,
                form.getNome(),
                form.getEmail(),
                form.getRole(),
                form.isAtivo()
        );
    }

    public void atualizar(Long id, AdminUtilizadorForm form) {
        validarForm(form, id);

        jdbcTemplate.update("""
                UPDATE utilizadores
                SET nome = ?,
                    email = ?,
                    role = ?,
                    ativo = ?
                WHERE id = ?
                """,
                form.getNome(),
                form.getEmail(),
                form.getRole(),
                form.isAtivo(),
                id
        );
    }

    public void alternarEstado(Long id) {
        jdbcTemplate.update("""
                UPDATE utilizadores
                SET ativo = NOT ativo
                WHERE id = ?
                """, id);
    }

    private void validarForm(AdminUtilizadorForm form, Long idAtual) {
        if (form.getNome() == null || form.getNome().isBlank()) {
            throw new IllegalArgumentException("O nome é obrigatório.");
        }

        if (form.getEmail() == null || form.getEmail().isBlank()) {
            throw new IllegalArgumentException("O email é obrigatório.");
        }

        if (!form.getEmail().contains("@")) {
            throw new IllegalArgumentException("O email não é válido.");
        }

        if (form.getRole() == null || form.getRole().isBlank()) {
            throw new IllegalArgumentException("A role é obrigatória.");
        }

        if (!List.of("ADMIN", "PROFESSOR", "ALUNO").contains(form.getRole())) {
            throw new IllegalArgumentException("A role indicada não é válida.");
        }

        if (emailJaExiste(form.getEmail(), idAtual)) {
            throw new IllegalArgumentException("Já existe um utilizador com esse email.");
        }
    }

    private boolean emailJaExiste(String email, Long idAtual) {
        Long total;

        if (idAtual == null) {
            total = jdbcTemplate.queryForObject("""
                    SELECT COUNT(*)
                    FROM utilizadores
                    WHERE email = ?
                    """, Long.class, email);
        } else {
            total = jdbcTemplate.queryForObject("""
                    SELECT COUNT(*)
                    FROM utilizadores
                    WHERE email = ?
                    AND id <> ?
                    """, Long.class, email, idAtual);
        }

        return total != null && total > 0;
    }

    private AdminUtilizadorDTO mapRow(java.sql.ResultSet rs, int rowNum) throws java.sql.SQLException {
        return new AdminUtilizadorDTO(
                rs.getLong("id"),
                rs.getString("nome"),
                rs.getString("email"),
                rs.getString("role"),
                rs.getBoolean("ativo"),
                toLocalDateTime(rs.getTimestamp("criado_em"))
        );
    }

    private LocalDateTime toLocalDateTime(Timestamp timestamp) {
        return timestamp != null ? timestamp.toLocalDateTime() : null;
    }
}