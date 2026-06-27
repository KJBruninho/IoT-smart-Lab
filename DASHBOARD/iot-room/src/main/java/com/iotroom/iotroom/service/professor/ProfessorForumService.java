package com.iotroom.iotroom.service.professor;

import com.iotroom.iotroom.dto.professor.ForumRespostaFormDTO;
import com.iotroom.iotroom.dto.professor.ForumTopicoFormDTO;
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

    public List<ForumTopico> listarTopicos(Long utilizadorId, boolean admin) {
        if (admin) {
            return forumTopicoRepository.findAll();
        }

        return listarTopicos(utilizadorId);
    }

    public ForumTopico obterTopico(Long topicoId, Long professorId) {
        return forumTopicoRepository.findByIdAndCriadoPorId(topicoId, professorId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Tópico não encontrado para este professor."
                ));
    }

    public ForumTopico obterTopico(Long topicoId, Long utilizadorId, boolean admin) {
        if (admin) {
            return forumTopicoRepository.findById(topicoId)
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND,
                            "Tópico não encontrado."
                    ));
        }

        return obterTopico(topicoId, utilizadorId);
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

    public List<Grupo> listarGrupos(Long utilizadorId, boolean admin) {
        if (admin) {
            return grupoRepository.findAll();
        }

        return listarGrupos(utilizadorId);
    }

    public List<Experiencia> listarExperiencias(Long professorId) {
        return experienciaRepository.findByCriadoPorIdOrderByCriadoEmDesc(professorId);
    }

    public List<Experiencia> listarExperiencias(Long utilizadorId, boolean admin) {
        if (admin) {
            return experienciaRepository.findAll();
        }

        return listarExperiencias(utilizadorId);
    }

    public Map<Long, String> obterNomesGrupos(Long professorId) {
        return criarMapaGrupos(listarGrupos(professorId));
    }

    public Map<Long, String> obterNomesGrupos(Long utilizadorId, boolean admin) {
        return criarMapaGrupos(listarGrupos(utilizadorId, admin));
    }

    public Map<Long, String> obterNomesExperiencias(Long professorId) {
        return criarMapaExperiencias(listarExperiencias(professorId));
    }

    public Map<Long, String> obterNomesExperiencias(Long utilizadorId, boolean admin) {
        return criarMapaExperiencias(listarExperiencias(utilizadorId, admin));
    }

    @Transactional
    public ForumTopico criarTopico(Long professorId, ForumTopicoFormDTO form) {
        return criarTopico(professorId, false, form);
    }

    @Transactional
    public ForumTopico criarTopico(Long utilizadorId, boolean admin, ForumTopicoFormDTO form) {
        validarTopico(form, utilizadorId, admin);

        ForumTopico topico = new ForumTopico();
        topico.setTitulo(form.getTitulo().trim());
        topico.setMensagem(form.getMensagem().trim());
        topico.setCriadoPorId(utilizadorId);
        topico.setGrupoId(form.getGrupoId());
        topico.setExperienciaId(form.getExperienciaId());
        topico.setEstado("ABERTO");
        topico.setCriadoEm(LocalDateTime.now());

        return forumTopicoRepository.save(topico);
    }

    @Transactional
    public ForumTopico atualizarTopico(Long topicoId, Long professorId, ForumTopicoFormDTO form) {
        return atualizarTopico(topicoId, professorId, false, form);
    }

    @Transactional
    public ForumTopico atualizarTopico(
            Long topicoId,
            Long utilizadorId,
            boolean admin,
            ForumTopicoFormDTO form
    ) {
        validarTopico(form, utilizadorId, admin);

        ForumTopico topico = obterTopico(topicoId, utilizadorId, admin);

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
        return responder(topicoId, professorId, false, form);
    }

    @Transactional
    public ForumResposta responder(
            Long topicoId,
            Long utilizadorId,
            boolean admin,
            ForumRespostaFormDTO form
    ) {
        ForumTopico topico = obterTopico(topicoId, utilizadorId, admin);

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
        resposta.setAutorId(utilizadorId);
        resposta.setMensagem(form.getMensagem().trim());
        resposta.setAtivo(true);
        resposta.setCriadoEm(LocalDateTime.now());

        return forumRespostaRepository.save(resposta);
    }

    @Transactional
    public void fecharTopico(Long topicoId, Long professorId) {
        fecharTopico(topicoId, professorId, false);
    }

    @Transactional
    public void fecharTopico(Long topicoId, Long utilizadorId, boolean admin) {
        ForumTopico topico = obterTopico(topicoId, utilizadorId, admin);

        topico.setEstado("FECHADO");
        topico.setFechadoEm(LocalDateTime.now());
        topico.setAtualizadoEm(LocalDateTime.now());

        forumTopicoRepository.save(topico);
    }

    @Transactional
    public void reabrirTopico(Long topicoId, Long professorId) {
        reabrirTopico(topicoId, professorId, false);
    }

    @Transactional
    public void reabrirTopico(Long topicoId, Long utilizadorId, boolean admin) {
        ForumTopico topico = obterTopico(topicoId, utilizadorId, admin);

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

    private Map<Long, String> criarMapaGrupos(List<Grupo> grupos) {
        Map<Long, String> mapa = new LinkedHashMap<>();

        for (Grupo grupo : grupos) {
            mapa.put(grupo.getId(), grupo.getNome());
        }

        return mapa;
    }

    private Map<Long, String> criarMapaExperiencias(List<Experiencia> experiencias) {
        Map<Long, String> mapa = new LinkedHashMap<>();

        for (Experiencia experiencia : experiencias) {
            mapa.put(experiencia.getId(), experiencia.getNome());
        }

        return mapa;
    }

    private void validarTopico(ForumTopicoFormDTO form, Long utilizadorId, boolean admin) {
        if (form.getTitulo() == null || form.getTitulo().trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O título é obrigatório.");
        }

        if (form.getTitulo().trim().length() > 150) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O título não pode ter mais de 150 caracteres.");
        }

        if (form.getMensagem() == null || form.getMensagem().trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A mensagem é obrigatória.");
        }

        if (form.getGrupoId() != null) {
            boolean grupoValido = admin
                    ? grupoRepository.existsById(form.getGrupoId())
                    : grupoRepository.existsByIdAndProfessorId(form.getGrupoId(), utilizadorId);

            if (!grupoValido) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        admin
                                ? "O grupo selecionado não existe."
                                : "O grupo selecionado não pertence a este professor."
                );
            }
        }

        if (form.getExperienciaId() != null) {
            boolean experienciaValida = admin
                    ? experienciaRepository.existsById(form.getExperienciaId())
                    : experienciaRepository.existsByIdAndCriadoPorId(form.getExperienciaId(), utilizadorId);

            if (!experienciaValida) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        admin
                                ? "A experiência selecionada não existe."
                                : "A experiência selecionada não pertence a este professor."
                );
            }
        }
    }
}