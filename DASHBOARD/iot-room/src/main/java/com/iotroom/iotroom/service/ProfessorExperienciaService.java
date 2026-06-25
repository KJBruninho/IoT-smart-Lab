package com.iotroom.iotroom.service;

import com.iotroom.iotroom.dto.ExperienciaEstacaoFormDTO;
import com.iotroom.iotroom.model.Estacao;
import com.iotroom.iotroom.model.ExperienciaEstacao;
import com.iotroom.iotroom.model.ExperienciaEstacaoId;
import com.iotroom.iotroom.repository.EstacaoRepository;
import com.iotroom.iotroom.repository.ExperienciaEstacaoRepository;
import com.iotroom.iotroom.dto.ExperienciaFormDTO;
import com.iotroom.iotroom.model.Experiencia;
import com.iotroom.iotroom.model.Grupo;
import com.iotroom.iotroom.repository.ExperienciaRepository;
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
        return experienciaRepository.findByCriadoPorIdOrderByCriadoEmDesc(professorId);
    }

    public Experiencia obterExperienciaDoProfessor(Long experienciaId, Long professorId) {
        return experienciaRepository.findByIdAndCriadoPorId(experienciaId, professorId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Experiência não encontrada para este professor."
                ));
    }

    public List<Grupo> listarGruposDisponiveis(Long professorId) {
        return grupoRepository.findByProfessorIdOrderByCriadoEmDesc(professorId);
    }

    public Map<Long, String> obterNomesDosGrupos(Long professorId) {
        List<Grupo> grupos = listarGruposDisponiveis(professorId);

        Map<Long, String> nomes = new LinkedHashMap<>();

        for (Grupo grupo : grupos) {
            nomes.put(grupo.getId(), grupo.getNome());
        }

        return nomes;
    }

    @Transactional
    public Experiencia criarExperiencia(Long professorId, ExperienciaFormDTO form) {
        validarFormulario(form, professorId);

        Experiencia experiencia = new Experiencia();
        experiencia.setNome(form.getNome().trim());
        experiencia.setDescricao(normalizarTexto(form.getDescricao()));
        experiencia.setGrupoId(form.getGrupoId());
        experiencia.setCriadoPorId(professorId);
        experiencia.setEstado("CRIADA");
        experiencia.setDataInicio(LocalDateTime.now());
        experiencia.setCriadoEm(LocalDateTime.now());

        return experienciaRepository.save(experiencia);
    }

    @Transactional
    public Experiencia atualizarExperiencia(Long experienciaId, Long professorId, ExperienciaFormDTO form) {
        validarFormulario(form, professorId);

        Experiencia experiencia = obterExperienciaDoProfessor(experienciaId, professorId);

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
        Experiencia experiencia = obterExperienciaDoProfessor(experienciaId, professorId);

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
        Experiencia experiencia = obterExperienciaDoProfessor(experienciaId, professorId);

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
        Experiencia experiencia = obterExperienciaDoProfessor(experienciaId, professorId);

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

    private void validarFormulario(ExperienciaFormDTO form, Long professorId) {
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

        boolean grupoPertenceAoProfessor = grupoRepository.existsByIdAndProfessorId(
                form.getGrupoId(),
                professorId
        );

        if (!grupoPertenceAoProfessor) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "O grupo selecionado não pertence a este professor."
            );
        }
    }
    
    public List<ExperienciaEstacao> listarEstacoesDaExperiencia(Long experienciaId, Long professorId) {
        obterExperienciaDoProfessor(experienciaId, professorId);

        return experienciaEstacaoRepository
                .findByIdExperienciaIdOrderByOrdemAscAdicionadaEmAsc(experienciaId);
    }

    public List<Estacao> listarEstacoesDisponiveisParaExperiencia(Long experienciaId, Long professorId) {
        Experiencia experiencia = obterExperienciaDoProfessor(experienciaId, professorId);

        List<Long> ids = experienciaEstacaoRepository
                .findEstacaoIdsDisponiveisParaGrupo(experiencia.getGrupoId());

        return estacaoRepository.findAllById(ids);
    }

    public Map<Long, Estacao> obterMapaEstacoes(Long experienciaId, Long professorId) {
        List<Estacao> estacoes = listarEstacoesDisponiveisParaExperiencia(experienciaId, professorId);

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
        Experiencia experiencia = obterExperienciaDoProfessor(experienciaId, professorId);

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
        Experiencia experiencia = obterExperienciaDoProfessor(experienciaId, professorId);

        if ("FINALIZADA".equals(experiencia.getEstado()) || "CANCELADA".equals(experiencia.getEstado())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Não é possível remover estações de uma experiência finalizada ou cancelada."
            );
        }

        experienciaEstacaoRepository.deleteByIdExperienciaIdAndIdEstacaoId(experienciaId, estacaoId);
    }

    private String normalizarTexto(String texto) {
        if (texto == null || texto.trim().isEmpty()) {
            return null;
        }

        return texto.trim();
    }
}