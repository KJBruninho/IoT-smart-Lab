package com.iotroom.iotroom.service;

import com.iotroom.iotroom.dto.GrupoFormDTO;
import com.iotroom.iotroom.model.Grupo;
import com.iotroom.iotroom.repository.GrupoRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ProfessorGrupoService {

    private final GrupoRepository grupoRepository;

    public ProfessorGrupoService(GrupoRepository grupoRepository) {
        this.grupoRepository = grupoRepository;
    }

    public List<Grupo> listarGruposDoProfessor(Long professorId) {
        return grupoRepository.findByProfessorIdOrderByCriadoEmDesc(professorId);
    }

    public Grupo obterGrupoDoProfessor(Long grupoId, Long professorId) {
        return grupoRepository.findByIdAndProfessorId(grupoId, professorId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Grupo não encontrado para este professor."
                ));
    }

    @Transactional
    public Grupo criarGrupo(Long professorId, GrupoFormDTO form) {
        validarFormulario(form);

        Grupo grupo = new Grupo();
        grupo.setNome(form.getNome().trim());
        grupo.setDescricao(normalizarTexto(form.getDescricao()));
        grupo.setProfessorId(professorId);
        grupo.setAtivo(true);
        grupo.setCriadoEm(LocalDateTime.now());

        return grupoRepository.save(grupo);
    }

    @Transactional
    public Grupo atualizarGrupo(Long grupoId, Long professorId, GrupoFormDTO form) {
        validarFormulario(form);

        Grupo grupo = obterGrupoDoProfessor(grupoId, professorId);

        grupo.setNome(form.getNome().trim());
        grupo.setDescricao(normalizarTexto(form.getDescricao()));
        grupo.setAtivo(Boolean.TRUE.equals(form.getAtivo()));
        grupo.setAtualizadoEm(LocalDateTime.now());

        return grupoRepository.save(grupo);
    }

    @Transactional
    public void alternarEstado(Long grupoId, Long professorId) {
        Grupo grupo = obterGrupoDoProfessor(grupoId, professorId);

        boolean ativoAtual = Boolean.TRUE.equals(grupo.getAtivo());

        grupo.setAtivo(!ativoAtual);
        grupo.setAtualizadoEm(LocalDateTime.now());

        grupoRepository.save(grupo);
    }

    public GrupoFormDTO criarFormVazio() {
        return new GrupoFormDTO(null, "", "", true);
    }

    public GrupoFormDTO criarFormAPartirDeGrupo(Grupo grupo) {
        return new GrupoFormDTO(
                grupo.getId(),
                grupo.getNome(),
                grupo.getDescricao(),
                grupo.getAtivo()
        );
    }

    private void validarFormulario(GrupoFormDTO form) {
        if (form.getNome() == null || form.getNome().trim().isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "O nome do grupo é obrigatório."
            );
        }

        if (form.getNome().trim().length() > 100) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "O nome do grupo não pode ter mais de 100 caracteres."
            );
        }
    }

    private String normalizarTexto(String texto) {
        if (texto == null || texto.trim().isEmpty()) {
            return null;
        }

        return texto.trim();
    }
}