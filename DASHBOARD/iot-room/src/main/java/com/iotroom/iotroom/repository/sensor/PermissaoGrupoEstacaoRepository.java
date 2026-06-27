package com.iotroom.iotroom.repository.sensor;

import com.iotroom.iotroom.model.PermissaoGrupoEstacao;
import com.iotroom.iotroom.model.PermissaoGrupoEstacaoId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PermissaoGrupoEstacaoRepository extends JpaRepository<PermissaoGrupoEstacao, PermissaoGrupoEstacaoId> {

    List<PermissaoGrupoEstacao> findByGrupo_Id(Long grupoId);

    void deleteByGrupo_Id(Long grupoId);

    boolean existsByGrupo_IdAndEstacao_Id(Long grupoId, Long estacaoId);
}