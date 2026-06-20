package com.iotroom.iotroom.repository;

import com.iotroom.iotroom.model.Estacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface EstacaoRepository extends JpaRepository<Estacao, Long> {

    @Query(value = """
            SELECT DISTINCT e.*
            FROM estacoes e
            INNER JOIN permissoes_utilizador_estacao pue ON pue.estacao_id = e.id
            WHERE pue.utilizador_id = :professorId
              AND e.ativa = TRUE
            ORDER BY e.nome ASC
            """, nativeQuery = true)
    List<Estacao> findEstacoesDoProfessor(@Param("professorId") Long professorId);
}