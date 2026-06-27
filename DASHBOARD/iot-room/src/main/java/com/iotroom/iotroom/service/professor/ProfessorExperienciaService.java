package com.iotroom.iotroom.service.professor;

import com.iotroom.iotroom.dto.professor.ExperienciaEstacaoFormDTO;
import com.iotroom.iotroom.dto.professor.ExperienciaFormDTO;
import com.iotroom.iotroom.model.Estacao;
import com.iotroom.iotroom.model.Experiencia;
import com.iotroom.iotroom.model.ExperienciaEstacao;
import com.iotroom.iotroom.model.ExperienciaEstacaoId;
import com.iotroom.iotroom.model.Grupo;
import com.iotroom.iotroom.repository.ExperienciaEstacaoRepository;
import com.iotroom.iotroom.repository.ExperienciaRepository;
import com.iotroom.iotroom.repository.GrupoRepository;
import com.iotroom.iotroom.repository.sensor.EstacaoRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class ProfessorExperienciaService {

    private final ExperienciaEstacaoRepository experienciaEstacaoRepository;
    private final EstacaoRepository estacaoRepository;
    private final ExperienciaRepository experienciaRepository;
    private final GrupoRepository grupoRepository;

    public ProfessorExperienciaService(
            ExperienciaRepository experienciaRepository,
            GrupoRepository grupoRepository,
            ExperienciaEstacaoRepository experienciaEstacaoRepository,
            EstacaoRepository estacaoRepository
    ) {
        this.experienciaRepository = experienciaRepository;
        this.grupoRepository = grupoRepository;
        this.experienciaEstacaoRepository = experienciaEstacaoRepository;
        this.estacaoRepository = estacaoRepository;
    }

    public List<Experiencia> listarExperienciasDoProfessor(Long professorId) {
        return listarExperienciasDoProfessor(professorId, false);
    }

    public List<Experiencia> listarExperienciasDoProfessor(Long utilizadorId, boolean admin) {
        if (admin) {
            return experienciaRepository.findAll();
        }

        return experienciaRepository.findByCriadoPorIdOrderByCriadoEmDesc(utilizadorId);
    }

    public Experiencia obterExperienciaDoProfessor(Long experienciaId, Long professorId) {
        return obterExperienciaDoProfessor(experienciaId, professorId, false);
    }

    public Experiencia obterExperienciaDoProfessor(Long experienciaId, Long utilizadorId, boolean admin) {
        if (admin) {
            return experienciaRepository.findById(experienciaId)
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND,
                            "Experiência não encontrada."
                    ));
        }

        return experienciaRepository.findByIdAndCriadoPorId(experienciaId, utilizadorId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Experiência não encontrada para este professor."
                ));
    }

    public List<Grupo> listarGruposDisponiveis(Long professorId) {
        return listarGruposDisponiveis(professorId, false);
    }

    public List<Grupo> listarGruposDisponiveis(Long utilizadorId, boolean admin) {
        if (admin) {
            return grupoRepository.findAll();
        }

        return grupoRepository.findByProfessorIdOrderByCriadoEmDesc(utilizadorId);
    }

    public Map<Long, String> obterNomesDosGrupos(Long professorId) {
        return obterNomesDosGrupos(professorId, false);
    }

    public Map<Long, String> obterNomesDosGrupos(Long utilizadorId, boolean admin) {
        Map<Long, String> nomes = new LinkedHashMap<>();

        for (Grupo grupo : listarGruposDisponiveis(utilizadorId, admin)) {
            nomes.put(grupo.getId(), grupo.getNome());
        }

        return nomes;
    }

    @Transactional
    public Experiencia criarExperiencia(Long professorId, ExperienciaFormDTO form) {
        return criarExperiencia(professorId, false, form);
    }

    @Transactional
    public Experiencia criarExperiencia(Long utilizadorId, boolean admin, ExperienciaFormDTO form) {
        validarFormulario(form, utilizadorId, admin);

        Experiencia experiencia = new Experiencia();
        experiencia.setNome(form.getNome().trim());
        experiencia.setDescricao(normalizarTexto(form.getDescricao()));
        experiencia.setGrupoId(form.getGrupoId());
        experiencia.setCriadoPorId(utilizadorId);
        experiencia.setEstado("CRIADA");
        experiencia.setDataInicio(LocalDateTime.now());
        experiencia.setCriadoEm(LocalDateTime.now());

        return experienciaRepository.save(experiencia);
    }

    @Transactional
    public Experiencia atualizarExperiencia(Long experienciaId, Long professorId, ExperienciaFormDTO form) {
        return atualizarExperiencia(experienciaId, professorId, false, form);
    }

    @Transactional
    public Experiencia atualizarExperiencia(
            Long experienciaId,
            Long utilizadorId,
            boolean admin,
            ExperienciaFormDTO form
    ) {
        validarFormulario(form, utilizadorId, admin);

        Experiencia experiencia = obterExperienciaDoProfessor(experienciaId, utilizadorId, admin);

        if ("FINALIZADA".equals(experiencia.getEstado()) || "CANCELADA".equals(experiencia.getEstado())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Não é possível editar uma experiência finalizada ou cancelada."
            );
        }

        experiencia.setNome(form.getNome().trim());
        experiencia.setDescricao(normalizarTexto(form.getDescricao()));
        experiencia.setGrupoId(form.getGrupoId());
        experiencia.setAtualizadoEm(LocalDateTime.now());

        return experienciaRepository.save(experiencia);
    }

    @Transactional
    public void iniciarExperiencia(Long experienciaId, Long professorId) {
        iniciarExperiencia(experienciaId, professorId, false);
    }

    @Transactional
    public void iniciarExperiencia(Long experienciaId, Long utilizadorId, boolean admin) {
        Experiencia experiencia = obterExperienciaDoProfessor(experienciaId, utilizadorId, admin);

        if (!"CRIADA".equals(experiencia.getEstado())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Só é possível iniciar experiências no estado CRIADA."
            );
        }

        experiencia.setEstado("EM_EXECUCAO");
        experiencia.setAtualizadoEm(LocalDateTime.now());

        experienciaRepository.save(experiencia);
    }

    @Transactional
    public void finalizarExperiencia(Long experienciaId, Long professorId) {
        finalizarExperiencia(experienciaId, professorId, false);
    }

    @Transactional
    public void finalizarExperiencia(Long experienciaId, Long utilizadorId, boolean admin) {
        Experiencia experiencia = obterExperienciaDoProfessor(experienciaId, utilizadorId, admin);

        if (!"CRIADA".equals(experiencia.getEstado()) && !"EM_EXECUCAO".equals(experiencia.getEstado())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Só é possível finalizar experiências criadas ou em execução."
            );
        }

        experiencia.setEstado("FINALIZADA");
        experiencia.setDataFim(LocalDateTime.now());
        experiencia.setAtualizadoEm(LocalDateTime.now());

        experienciaRepository.save(experiencia);
    }

    @Transactional
    public void cancelarExperiencia(Long experienciaId, Long professorId) {
        cancelarExperiencia(experienciaId, professorId, false);
    }

    @Transactional
    public void cancelarExperiencia(Long experienciaId, Long utilizadorId, boolean admin) {
        Experiencia experiencia = obterExperienciaDoProfessor(experienciaId, utilizadorId, admin);

        if ("FINALIZADA".equals(experiencia.getEstado()) || "CANCELADA".equals(experiencia.getEstado())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Esta experiência já está terminada."
            );
        }

        experiencia.setEstado("CANCELADA");
        experiencia.setDataFim(LocalDateTime.now());
        experiencia.setAtualizadoEm(LocalDateTime.now());

        experienciaRepository.save(experiencia);
    }

    public ExperienciaFormDTO criarFormVazio() {
        return new ExperienciaFormDTO(null, "", "", null);
    }

    public ExperienciaFormDTO criarFormAPartirDeExperiencia(Experiencia experiencia) {
        return new ExperienciaFormDTO(
                experiencia.getId(),
                experiencia.getNome(),
                experiencia.getDescricao(),
                experiencia.getGrupoId()
        );
    }

    public List<ExperienciaEstacao> listarEstacoesDaExperiencia(Long experienciaId, Long professorId) {
        return listarEstacoesDaExperiencia(experienciaId, professorId, false);
    }

    public List<ExperienciaEstacao> listarEstacoesDaExperiencia(
            Long experienciaId,
            Long utilizadorId,
            boolean admin
    ) {
        obterExperienciaDoProfessor(experienciaId, utilizadorId, admin);

        return experienciaEstacaoRepository
                .findByIdExperienciaIdOrderByOrdemAscAdicionadaEmAsc(experienciaId);
    }

    public List<Estacao> listarEstacoesDisponiveisParaExperiencia(Long experienciaId, Long professorId) {
        return listarEstacoesDisponiveisParaExperiencia(experienciaId, professorId, false);
    }

    public List<Estacao> listarEstacoesDisponiveisParaExperiencia(
            Long experienciaId,
            Long utilizadorId,
            boolean admin
    ) {
        Experiencia experiencia = obterExperienciaDoProfessor(experienciaId, utilizadorId, admin);

        if (admin) {
            return estacaoRepository.findAll();
        }

        List<Long> ids = experienciaEstacaoRepository
                .findEstacaoIdsDisponiveisParaGrupo(experiencia.getGrupoId());

        return estacaoRepository.findAllById(ids);
    }

    public Map<Long, Estacao> obterMapaEstacoes(Long experienciaId, Long professorId) {
        return obterMapaEstacoes(experienciaId, professorId, false);
    }

    public Map<Long, Estacao> obterMapaEstacoes(Long experienciaId, Long utilizadorId, boolean admin) {
        List<Estacao> estacoes = listarEstacoesDisponiveisParaExperiencia(experienciaId, utilizadorId, admin);

        Map<Long, Estacao> mapa = new LinkedHashMap<>();

        for (Estacao estacao : estacoes) {
            mapa.put(estacao.getId(), estacao);
        }

        return mapa;
    }

    @Transactional
    public void associarEstacao(
            Long experienciaId,
            Long professorId,
            ExperienciaEstacaoFormDTO form
    ) {
        associarEstacao(experienciaId, professorId, false, form);
    }

    @Transactional
    public void associarEstacao(
            Long experienciaId,
            Long utilizadorId,
            boolean admin,
            ExperienciaEstacaoFormDTO form
    ) {
        Experiencia experiencia = obterExperienciaDoProfessor(experienciaId, utilizadorId, admin);

        if ("FINALIZADA".equals(experiencia.getEstado()) || "CANCELADA".equals(experiencia.getEstado())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Não é possível alterar estações de uma experiência finalizada ou cancelada."
            );
        }

        if (form.getEstacaoId() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "É obrigatório selecionar uma estação."
            );
        }

        boolean estacaoExiste = estacaoRepository.existsById(form.getEstacaoId());

        if (!estacaoExiste) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "A estação selecionada não existe."
            );
        }

        if (!admin) {
            long permissoes = experienciaEstacaoRepository.countPermissaoGrupoEstacao(
                    experiencia.getGrupoId(),
                    form.getEstacaoId()
            );

            if (permissoes == 0) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Esta estação não está permitida para o grupo desta experiência."
                );
            }
        }

        boolean jaAssociada = experienciaEstacaoRepository
                .existsByIdExperienciaIdAndIdEstacaoId(experienciaId, form.getEstacaoId());

        if (jaAssociada) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Esta estação já está associada à experiência."
            );
        }

        ExperienciaEstacao associacao = new ExperienciaEstacao();
        associacao.setId(new ExperienciaEstacaoId(experienciaId, form.getEstacaoId()));
        associacao.setOrdem(form.getOrdem() != null ? form.getOrdem() : 1);
        associacao.setObservacao(normalizarTexto(form.getObservacao()));
        associacao.setAdicionadaEm(LocalDateTime.now());

        experienciaEstacaoRepository.save(associacao);
    }

    @Transactional
    public void removerEstacao(Long experienciaId, Long estacaoId, Long professorId) {
        removerEstacao(experienciaId, estacaoId, professorId, false);
    }

    @Transactional
    public void removerEstacao(Long experienciaId, Long estacaoId, Long utilizadorId, boolean admin) {
        Experiencia experiencia = obterExperienciaDoProfessor(experienciaId, utilizadorId, admin);

        if ("FINALIZADA".equals(experiencia.getEstado()) || "CANCELADA".equals(experiencia.getEstado())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Não é possível remover estações de uma experiência finalizada ou cancelada."
            );
        }

        experienciaEstacaoRepository.deleteByIdExperienciaIdAndIdEstacaoId(experienciaId, estacaoId);
    }

    private void validarFormulario(ExperienciaFormDTO form, Long utilizadorId, boolean admin) {
        if (form.getNome() == null || form.getNome().trim().isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "O nome da experiência é obrigatório."
            );
        }

        if (form.getNome().trim().length() > 120) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "O nome da experiência não pode ter mais de 120 caracteres."
            );
        }

        if (form.getGrupoId() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "É obrigatório selecionar um grupo."
            );
        }

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

    private String normalizarTexto(String texto) {
        if (texto == null || texto.trim().isEmpty()) {
            return null;
        }

        return texto.trim();
    }
}