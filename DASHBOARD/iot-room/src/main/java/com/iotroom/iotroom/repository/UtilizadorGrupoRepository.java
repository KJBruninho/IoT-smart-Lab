package com.iotroom.iotroom.repository;

import com.iotroom.iotroom.dto.GrupoMembroProjection;
import com.iotroom.iotroom.model.UtilizadorGrupo;
import com.iotroom.iotroom.model.UtilizadorGrupoId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UtilizadorGrupoRepository extends JpaRepository<UtilizadorGrupo, UtilizadorGrupoId> {

    @Query(value = """
            SELECT
                u.id AS utilizadorId,
                u.nome AS nome,
                u.email AS email,
                rg.id AS roleGrupoId,
                rg.nome AS roleGrupo,
                ug.criado_em AS criadoEm
            FROM utilizador_grupos ug
            INNER JOIN utilizadores u ON u.id = ug.utilizador_id
            INNER JOIN roles_grupo rg ON rg.id = ug.role_grupo_id
            WHERE ug.grupo_id = :grupoId
            ORDER BY
                CASE rg.nome
                    WHEN 'OWNER' THEN 1
                    WHEN 'MANAGER' THEN 2
                    WHEN 'OPERATOR' THEN 3
                    WHEN 'VIEWER' THEN 4
                    ELSE 5
                END,
                u.nome ASC
            """, nativeQuery = true)
    List<GrupoMembroProjection> listarMembrosDoGrupo(@Param("grupoId") Long grupoId);

    long countByIdGrupoIdAndRoleGrupoId(Long grupoId, Long roleGrupoId);

    boolean existsByIdUtilizadorIdAndIdGrupoId(Long utilizadorId, Long grupoId);

    void deleteByIdUtilizadorIdAndIdGrupoId(Long utilizadorId, Long grupoId);
}