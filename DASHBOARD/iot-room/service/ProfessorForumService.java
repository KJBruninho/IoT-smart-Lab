package com.iotroom.iotroom.service;

import com.iotroom.iotroom.dto.ForumRespostaFormDTO;
import com.iotroom.iotroom.dto.ForumTopicoFormDTO;
import com.iotroom.iotroom.model.Experiencia;
import com.iotroom.iotroom.model.ForumResposta;
import com.iotroom.iotroom.model.ForumTopico;
import com.iotroom.iotroom.model.Grupo;
import com.iotroom.iotroom.repository.ExperienciaRepository;
import com.iotroom.iotroom.repository.ForumRespostaRepository;
import com.iotroom.iotroom.repository.ForumTopicoRepository;
import com.iotroom.iotroom.repository.GrupoRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class ProfessorForumService {

    private final ForumTopicoRepository forumTopicoRepository;
    private final ForumRespostaRepository forumRespostaRepository;
    private final GrupoRepository grupoRepository;
    private final ExperienciaRepository experienciaRepository;

    public ProfessorForumService(
            ForumTopicoRepository forumTopicoRepository,
            ForumRespostaRepository forumRespostaRepository,
            GrupoRepository grupoRepository,
            ExperienciaRepository experienciaRepository
    ) {
        this.forumTopicoRepository = forumTopicoRepository;
        this.forumRespostaRepository = forumRespostaRepository;
        this.grupoRepository = grupoRepository;
        this.experienciaRepository = experienciaRepository;
    }

    public List<ForumTopico> listarTopicos(Long professorId) {
        return forumTopicoRepository.findByCriadoPorIdOrderByCriadoEmDesc(professorId);
    }

    public ForumTopico obterTopico(Long topicoId, Long professorId) {
        return forumTopicoRepository.findByIdAndCriadoPorId(topicoId, professorId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Tópico não encontrado para este professor."
                ));
    }

    public List<ForumResposta> listarRespostas(Long topicoId) {
        return forumRespostaRepository.findByTopicoIdAndAtivoTrueOrderByCriadoEmAsc(topicoId);
    }

    public long contarRespostas(Long topicoId) {
        return forumRespostaRepository.countByTopicoIdAndAtivoTrue(topicoId);
    }

    public List<Grupo> listarGrupos(Long professorId) {
        return grupoRepository.findByProfessorIdOrderByCriadoEmDesc(professorId);
    }

    public List<Experiencia> listarExperiencias(Long professorId) {
        return experienciaRepository.findByCriadoPorIdOrderByCriadoEmDesc(professorId);
    }

    public Map<Long, String> obterNomesGrupos(Long professorId) {
        Map<Long, String> mapa = new LinkedHashMap<>();

        for (Grupo grupo : listarGrupos(professorId)) {
            mapa.put(grupo.getId(), grupo.getNome());
        }

        return mapa;
    }

    public Map<Long, String> obterNomesExperiencias(Long professorId) {
        Map<Long, String> mapa = new LinkedHashMap<>();

        for (Experiencia experiencia : listarExperiencias(professorId)) {
            mapa.put(experiencia.getId(), experiencia.getNome());
        }

        return mapa;
    }

    @Transactional
    public ForumTopico criarTopico(Long professorId, ForumTopicoFormDTO form) {
        validarTopico(form, professorId);

        ForumTopico topico = new ForumTopico();
        topico.setTitulo(form.getTitulo().trim());
        topico.setMensagem(form.getMensagem().trim());
        topico.setCriadoPorId(professorId);
        topico.setGrupoId(form.getGrupoId());
        topico.setExperienciaId(form.getExperienciaId());
        topico.setEstado("ABERTO");
        topico.setCriadoEm(LocalDateTime.now());

        return forumTopicoRepository.save(topico);
    }

    @Transactional
    public ForumTopico atualizarTopico(Long topicoId, Long professorId, ForumTopicoFormDTO form) {
        validarTopico(form, professorId);

        ForumTopico topico = obterTopico(topicoId, professorId);

        if ("FECHADO".equals(topico.getEstado())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Não é possível editar um tópico fechado."
            );
        }

        topico.setTitulo(form.getTitulo().trim());
        topico.setMensagem(form.getMensagem().trim());
        topico.setGrupoId(form.getGrupoId());
        topico.setExperienciaId(form.getExperienciaId());
        topico.setAtualizadoEm(LocalDateTime.now());

        return forumTopicoRepository.save(topico);
    }

    @Transactional
    public ForumResposta responder(Long topicoId, Long professorId, ForumRespostaFormDTO form) {
        ForumTopico topico = obterTopico(topicoId, professorId);

        if ("FECHADO".equals(topico.getEstado())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Não é possível responder a um tópico fechado."
            );
        }

        if (form.getMensagem() == null || form.getMensagem().trim().isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "A resposta não pode estar vazia."
            );
        }

        ForumResposta resposta = new ForumResposta();
        resposta.setTopicoId(topicoId);
        resposta.setAutorId(professorId);
        resposta.setMensagem(form.getMensagem().trim());
        resposta.setAtivo(true);
        resposta.setCriadoEm(LocalDateTime.now());

        return forumRespostaRepository.save(resposta);
    }

    @Transactional
    public void fecharTopico(Long topicoId, Long professorId) {
        ForumTopico topico = obterTopico(topicoId, professorId);

        topico.setEstado("FECHADO");
        topico.setFechadoEm(LocalDateTime.now());
        topico.setAtualizadoEm(LocalDateTime.now());

        forumTopicoRepository.save(topico);
    }

    @Transactional
    public void reabrirTopico(Long topicoId, Long professorId) {
        ForumTopico topico = obterTopico(topicoId, professorId);

        topico.setEstado("ABERTO");
        topico.setFechadoEm(null);
        topico.setAtualizadoEm(LocalDateTime.now());

        forumTopicoRepository.save(topico);
    }

    public ForumTopicoFormDTO criarFormVazio() {
        return new ForumTopicoFormDTO(null, "", "", null, null);
    }

    public ForumTopicoFormDTO criarFormAPartirDeTopico(ForumTopico topico) {
        return new ForumTopicoFormDTO(
                topico.getId(),
                topico.getTitulo(),
                topico.getMensagem(),
                topico.getGrupoId(),
                topico.getExperienciaId()
        );
    }

    private void validarTopico(ForumTopicoFormDTO form, Long professorId) {
        if (form.getTitulo() == null || form.getTitulo().trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O título é obrigatório.");
        }

        if (form.getTitulo().trim().length() > 150) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O título não pode ter mais de 150 caracteres.");
        }

        if (form.getMensagem() == null || form.getMensagem().trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A mensagem é obrigatória.");
        }

        if (form.getGrupoId() != null &&
                !grupoRepository.existsByIdAndProfessorId(form.getGrupoId(), professorId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O grupo selecionado não pertence a este professor.");
        }

        if (form.getExperienciaId() != null &&
                !experienciaRepository.existsByIdAndCriadoPorId(form.getExperienciaId(), professorId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A experiência selecionada não pertence a este professor.");
        }
    }
}