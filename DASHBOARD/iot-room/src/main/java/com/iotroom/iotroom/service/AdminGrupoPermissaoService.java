package com.iotroom.iotroom.service;

import com.iotroom.iotroom.dto.AdminGrupoEstacaoPermissaoDTO;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Service
public class AdminGrupoPermissaoService {

    private final JdbcTemplate jdbcTemplate;

    public AdminGrupoPermissaoService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<AdminGrupoEstacaoPermissaoDTO> listarEstacoesDoGrupo(Long grupoId) {
        validarGrupoExiste(grupoId);

        return jdbcTemplate.query("""
                SELECT
                    e.id,
                    e.nome,
                    e.device_id,
                    e.localizacao,
                    e.ativa,
                    CASE
                        WHEN p.grupo_id IS NULL THEN FALSE
                        ELSE TRUE
                    END AS permitida,
                    COUNT(s.id) AS total_sensores
                FROM estacoes e
                LEFT JOIN permissoes_grupo_estacao p
                    ON p.estacao_id = e.id
                    AND p.grupo_id = ?
                LEFT JOIN sensores s
                    ON s.estacao_id = e.id
                GROUP BY
                    e.id,
                    e.nome,
                    e.device_id,
                    e.localizacao,
                    e.ativa,
                    p.grupo_id
                ORDER BY
                    permitida DESC,
                    e.ativa DESC,
                    e.nome ASC
                """,
                (rs, rowNum) -> new AdminGrupoEstacaoPermissaoDTO(
                        rs.getLong("id"),
                        rs.getString("nome"),
                        rs.getString("device_id"),
                        rs.getString("localizacao"),
                        rs.getBoolean("ativa"),
                        rs.getBoolean("permitida"),
                        rs.getLong("total_sensores")
                ),
                grupoId
        );
    }

    @Transactional
    public void atualizarEstacoesDoGrupo(Long grupoId, List<Long> estacaoIds) {
        validarGrupoExiste(grupoId);

        List<Long> idsLimpos = limparIds(estacaoIds);

        validarEstacoesExistem(idsLimpos);

        jdbcTemplate.update("""
                DELETE FROM permissoes_grupo_estacao
                WHERE grupo_id = ?
                """, grupoId);

        if (idsLimpos.isEmpty()) {
            return;
        }

        jdbcTemplate.batchUpdate("""
                INSERT INTO permissoes_grupo_estacao (grupo_id, estacao_id)
                VALUES (?, ?)
                """,
                new BatchPreparedStatementSetter() {
                    @Override
                    public void setValues(PreparedStatement ps, int i) throws SQLException {
                        ps.setLong(1, grupoId);
                        ps.setLong(2, idsLimpos.get(i));
                    }

                    @Override
                    public int getBatchSize() {
                        return idsLimpos.size();
                    }
                }
        );
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
        if (estacaoIds == null || estacaoIds.isEmpty()) {
            return;
        }

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
}