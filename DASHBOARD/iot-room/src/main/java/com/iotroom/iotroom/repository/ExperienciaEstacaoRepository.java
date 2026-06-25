package com.iotroom.iotroom.repository;

import com.iotroom.iotroom.model.ExperienciaEstacao;
import com.iotroom.iotroom.model.ExperienciaEstacaoId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ExperienciaEstacaoRepository extends JpaRepository<ExperienciaEstacao, ExperienciaEstacaoId> {

    List<ExperienciaEstacao> findByIdExperienciaIdOrderByOrdemAscAdicionadaEmAsc(Long experienciaId);

    boolean existsByIdExperienciaIdAndIdEstacaoId(Long experienciaId, Long estacaoId);

    void deleteByIdExperienciaIdAndIdEstacaoId(Long experienciaId, Long estacaoId);

    @Query(value = """
            SELECT COUNT(*)
            FROM permissoes_grupo_estacao
            WHERE grupo_id = :grupoId
              AND estacao_id = :estacaoId
            """, nativeQuery = true)
    long countPermissaoGrupoEstacao(
            @Param("grupoId") Long grupoId,
            @Param("estacaoId") Long estacaoId
    );

    @Query(value = """
            SELECT e.id
            FROM estacoes e
            INNER JOIN permissoes_grupo_estacao pge ON pge.estacao_id = e.id
            WHERE pge.grupo_id = :grupoId
              AND e.ativa = TRUE
            ORDER BY e.nome ASC
            """, nativeQuery = true)
    List<Long> findEstacaoIdsDisponiveisParaGrupo(@Param("grupoId") Long grupoId);
}