package com.iotroom.iotroom.service.professor;

import com.iotroom.iotroom.dto.professor.GrupoFormDTO;
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
        return listarGruposDoProfessor(professorId, false);
    }

    public List<Grupo> listarGruposDoProfessor(Long utilizadorId, boolean admin) {
        if (admin) {
            return grupoRepository.findAll();
        }

        return grupoRepository.findByProfessorIdOrderByCriadoEmDesc(utilizadorId);
    }

    public Grupo obterGrupoDoProfessor(Long grupoId, Long professorId) {
        return obterGrupoDoProfessor(grupoId, professorId, false);
    }

    public Grupo obterGrupoDoProfessor(Long grupoId, Long utilizadorId, boolean admin) {
        if (admin) {
            return grupoRepository.findById(grupoId)
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND,
                            "Grupo não encontrado."
                    ));
        }

        return grupoRepository.findByIdAndProfessorId(grupoId, utilizadorId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Grupo não encontrado para este professor."
                ));
    }

    @Transactional
    public Grupo criarGrupo(Long professorId, GrupoFormDTO form) {
        return criarGrupo(professorId, false, form);
    }

    @Transactional
    public Grupo criarGrupo(Long utilizadorId, boolean admin, GrupoFormDTO form) {
        validarFormulario(form);

        Grupo grupo = new Grupo();
        grupo.setNome(form.getNome().trim());
        grupo.setDescricao(normalizarTexto(form.getDescricao()));
        grupo.setProfessorId(utilizadorId);
        grupo.setAtivo(true);
        grupo.setCriadoEm(LocalDateTime.now());
        grupo.setAtualizadoEm(LocalDateTime.now());

        return grupoRepository.save(grupo);
    }

    @Transactional
    public Grupo atualizarGrupo(Long grupoId, Long professorId, GrupoFormDTO form) {
        return atualizarGrupo(grupoId, professorId, false, form);
    }

    @Transactional
    public Grupo atualizarGrupo(
            Long grupoId,
            Long utilizadorId,
            boolean admin,
            GrupoFormDTO form
    ) {
        validarFormulario(form);

        Grupo grupo = obterGrupoDoProfessor(grupoId, utilizadorId, admin);

        grupo.setNome(form.getNome().trim());
        grupo.setDescricao(normalizarTexto(form.getDescricao()));
        grupo.setAtivo(Boolean.TRUE.equals(form.getAtivo()));
        grupo.setAtualizadoEm(LocalDateTime.now());

        return grupoRepository.save(grupo);
    }

    @Transactional
    public void alternarEstado(Long grupoId, Long professorId) {
        alternarEstado(grupoId, professorId, false);
    }

    @Transactional
    public void alternarEstado(Long grupoId, Long utilizadorId, boolean admin) {
        Grupo grupo = obterGrupoDoProfessor(grupoId, utilizadorId, admin);

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
        if (form == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Formulário inválido."
            );
        }

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