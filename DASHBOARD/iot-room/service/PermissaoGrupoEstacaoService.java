package com.iotroom.iotroom.service;

import com.iotroom.iotroom.model.Estacao;
import com.iotroom.iotroom.model.Grupo;
import com.iotroom.iotroom.model.PermissaoGrupoEstacao;
import com.iotroom.iotroom.repository.EstacaoRepository;
import com.iotroom.iotroom.repository.GrupoRepository;
import com.iotroom.iotroom.repository.PermissaoGrupoEstacaoRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PermissaoGrupoEstacaoService {

    private final PermissaoGrupoEstacaoRepository permissaoRepository;
    private final GrupoRepository grupoRepository;
    private final EstacaoRepository estacaoRepository;

    public PermissaoGrupoEstacaoService(
            PermissaoGrupoEstacaoRepository permissaoRepository,
            GrupoRepository grupoRepository,
            EstacaoRepository estacaoRepository
    ) {
        this.permissaoRepository = permissaoRepository;
        this.grupoRepository = grupoRepository;
        this.estacaoRepository = estacaoRepository;
    }

    public List<Estacao> listarEstacoesAtivas() {
        return estacaoRepository.findByAtivaTrue();
    }

    public List<Long> listarIdsEstacoesComAcesso(Long grupoId) {
        return permissaoRepository.findByGrupo_Id(grupoId)
                .stream()
                .map(permissao -> permissao.getEstacao().getId())
                .toList();
    }

    @Transactional
    public void atualizarEstacoesDoGrupo(Long grupoId, List<Long> estacaoIds) {
        Grupo grupo = grupoRepository.findById(grupoId)
                .orElseThrow(() -> new RuntimeException("Grupo não encontrado"));

        permissaoRepository.deleteByGrupo_Id(grupoId);

        if (estacaoIds == null || estacaoIds.isEmpty()) {
            return;
        }

        for (Long estacaoId : estacaoIds) {
            Estacao estacao = estacaoRepository.findById(estacaoId)
                    .orElseThrow(() -> new RuntimeException("Estação não encontrada"));

            permissaoRepository.save(new PermissaoGrupoEstacao(grupo, estacao));
        }
    }
}