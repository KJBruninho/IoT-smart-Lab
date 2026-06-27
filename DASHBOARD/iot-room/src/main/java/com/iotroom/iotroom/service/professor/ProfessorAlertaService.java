package com.iotroom.iotroom.service.professor;

import com.iotroom.iotroom.dto.leitura.LeituraAlertaContextProjection;
import com.iotroom.iotroom.dto.professor.RegraAlertaSensorFormDTO;
import com.iotroom.iotroom.model.*;
import com.iotroom.iotroom.repository.*;
import com.iotroom.iotroom.repository.leitura.AlertaSensorRepository;
import com.iotroom.iotroom.repository.leitura.RegraAlertaSensorRepository;
import com.iotroom.iotroom.repository.sensor.EstacaoRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ProfessorAlertaService {

    private final RegraAlertaSensorRepository regraRepository;
    private final AlertaSensorRepository alertaRepository;
    private final GrupoRepository grupoRepository;
    private final ExperienciaRepository experienciaRepository;
    private final EstacaoRepository estacaoRepository;

    public ProfessorAlertaService(
            RegraAlertaSensorRepository regraRepository,
            AlertaSensorRepository alertaRepository,
            GrupoRepository grupoRepository,
            ExperienciaRepository experienciaRepository,
            EstacaoRepository estacaoRepository
    ) {
        this.regraRepository = regraRepository;
        this.alertaRepository = alertaRepository;
        this.grupoRepository = grupoRepository;
        this.experienciaRepository = experienciaRepository;
        this.estacaoRepository = estacaoRepository;
    }

    public List<AlertaSensor> listarAlertas(Long professorId) {
        return listarAlertas(professorId, false);
    }

    public List<AlertaSensor> listarAlertas(Long utilizadorId, boolean admin) {
        if (admin) {
            return alertaRepository.findAll();
        }

        return alertaRepository.findByProfessorIdOrderByCriadoEmDesc(utilizadorId);
    }

    public List<RegraAlertaSensor> listarRegras(Long professorId) {
        return listarRegras(professorId, false);
    }

    public List<RegraAlertaSensor> listarRegras(Long utilizadorId, boolean admin) {
        if (admin) {
            return regraRepository.findAll();
        }

        return regraRepository.findByProfessorIdOrderByCriadaEmDesc(utilizadorId);
    }

    public List<Grupo> listarGrupos(Long professorId) {
        return listarGrupos(professorId, false);
    }

    public List<Grupo> listarGrupos(Long utilizadorId, boolean admin) {
        if (admin) {
            return grupoRepository.findAll();
        }

        return grupoRepository.findByProfessorIdOrderByCriadoEmDesc(utilizadorId);
    }

    public List<Experiencia> listarExperiencias(Long professorId) {
        return listarExperiencias(professorId, false);
    }

    public List<Experiencia> listarExperiencias(Long utilizadorId, boolean admin) {
        if (admin) {
            return experienciaRepository.findAll();
        }

        return experienciaRepository.findByCriadoPorIdOrderByCriadoEmDesc(utilizadorId);
    }

    public List<Estacao> listarEstacoes(Long professorId) {
        return listarEstacoes(professorId, false);
    }

    public List<Estacao> listarEstacoes(Long utilizadorId, boolean admin) {
        if (admin) {
            return estacaoRepository.findAll();
        }

        return estacaoRepository.findEstacoesDoProfessor(utilizadorId);
    }

    public RegraAlertaSensor obterRegra(Long regraId, Long professorId) {
        return obterRegra(regraId, professorId, false);
    }

    public RegraAlertaSensor obterRegra(Long regraId, Long utilizadorId, boolean admin) {
        if (admin) {
            return regraRepository.findById(regraId)
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND,
                            "Regra de alerta não encontrada."
                    ));
        }

        return regraRepository.findByIdAndProfessorId(regraId, utilizadorId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Regra de alerta não encontrada."
                ));
    }

    @Transactional
    public RegraAlertaSensor criarRegra(Long professorId, RegraAlertaSensorFormDTO form) {
        return criarRegra(professorId, false, form);
    }

    @Transactional
    public RegraAlertaSensor criarRegra(
            Long utilizadorId,
            boolean admin,
            RegraAlertaSensorFormDTO form
    ) {
        validarFormulario(form, utilizadorId, admin);

        RegraAlertaSensor regra = new RegraAlertaSensor();

        preencherRegra(regra, utilizadorId, form);

        return regraRepository.save(regra);
    }

    @Transactional
    public RegraAlertaSensor atualizarRegra(Long regraId, Long professorId, RegraAlertaSensorFormDTO form) {
        return atualizarRegra(regraId, professorId, false, form);
    }

    @Transactional
    public RegraAlertaSensor atualizarRegra(
            Long regraId,
            Long utilizadorId,
            boolean admin,
            RegraAlertaSensorFormDTO form
    ) {
        validarFormulario(form, utilizadorId, admin);

        RegraAlertaSensor regra = obterRegra(regraId, utilizadorId, admin);

        preencherRegra(regra, utilizadorId, form);
        regra.setAtualizadaEm(LocalDateTime.now());

        return regraRepository.save(regra);
    }

    @Transactional
    public void alternarEstadoRegra(Long regraId, Long professorId) {
        alternarEstadoRegra(regraId, professorId, false);
    }

    @Transactional
    public void alternarEstadoRegra(Long regraId, Long utilizadorId, boolean admin) {
        RegraAlertaSensor regra = obterRegra(regraId, utilizadorId, admin);

        regra.setAtivo(!Boolean.TRUE.equals(regra.getAtivo()));
        regra.setAtualizadaEm(LocalDateTime.now());

        regraRepository.save(regra);
    }

    @Transactional
    public void marcarLido(Long alertaId, Long professorId) {
        marcarLido(alertaId, professorId, false);
    }

    @Transactional
    public void marcarLido(Long alertaId, Long utilizadorId, boolean admin) {
        AlertaSensor alerta = obterAlerta(alertaId, utilizadorId, admin);

        alerta.setEstado("LIDO");
        alerta.setLidoEm(LocalDateTime.now());

        alertaRepository.save(alerta);
    }

    @Transactional
    public void resolver(Long alertaId, Long professorId) {
        resolver(alertaId, professorId, false);
    }

    @Transactional
    public void resolver(Long alertaId, Long utilizadorId, boolean admin) {
        AlertaSensor alerta = obterAlerta(alertaId, utilizadorId, admin);

        alerta.setEstado("RESOLVIDO");
        alerta.setResolvidoEm(LocalDateTime.now());

        alertaRepository.save(alerta);
    }

    @Transactional
    public void ignorar(Long alertaId, Long professorId) {
        ignorar(alertaId, professorId, false);
    }

    @Transactional
    public void ignorar(Long alertaId, Long utilizadorId, boolean admin) {
        AlertaSensor alerta = obterAlerta(alertaId, utilizadorId, admin);

        alerta.setEstado("IGNORADO");

        alertaRepository.save(alerta);
    }

    public RegraAlertaSensorFormDTO criarFormVazio() {
        RegraAlertaSensorFormDTO form = new RegraAlertaSensorFormDTO();
        form.setTipoSensor("TEMPERATURA");
        form.setOperador("ACIMA_DE");
        form.setSeveridade("AVISO");
        form.setAtivo(true);
        form.setCooldownMinutos(10);
        return form;
    }

    public RegraAlertaSensorFormDTO criarFormAPartirDeRegra(RegraAlertaSensor regra) {
        RegraAlertaSensorFormDTO form = new RegraAlertaSensorFormDTO();

        form.setId(regra.getId());
        form.setGrupoId(regra.getGrupoId());
        form.setExperienciaId(regra.getExperienciaId());
        form.setEstacaoId(regra.getEstacaoId());
        form.setTipoSensor(regra.getTipoSensor());
        form.setOperador(regra.getOperador());
        form.setValorMin(regra.getValorMin());
        form.setValorMax(regra.getValorMax());
        form.setTitulo(regra.getTitulo());
        form.setMensagem(regra.getMensagem());
        form.setSeveridade(regra.getSeveridade());
        form.setAtivo(regra.getAtivo());
        form.setCooldownMinutos(regra.getCooldownMinutos());

        return form;
    }

    @Transactional
    public void processarLeitura(Long leituraId) {
        LeituraAlertaContextProjection contexto = alertaRepository.obterContextoLeitura(leituraId)
                .orElse(null);

        if (contexto == null) {
            return;
        }

        List<RegraAlertaSensor> regras = regraRepository.findRegrasAplicaveis(
                contexto.getProfessorId(),
                contexto.getGrupoId(),
                contexto.getExperienciaId(),
                contexto.getEstacaoId(),
                contexto.getTipoSensor()
        );

        for (RegraAlertaSensor regra : regras) {
            if (!regraDispara(regra, contexto.getValorLido())) {
                continue;
            }

            LocalDateTime limiteCooldown = LocalDateTime.now()
                    .minusMinutes(regra.getCooldownMinutos() != null ? regra.getCooldownMinutos() : 10);

            boolean existeRecente = alertaRepository.existsByRegraIdAndCriadoEmAfter(
                    regra.getId(),
                    limiteCooldown
            );

            if (existeRecente) {
                continue;
            }

            AlertaSensor alerta = new AlertaSensor();

            alerta.setRegraId(regra.getId());
            alerta.setLeituraId(contexto.getLeituraId());
            alerta.setProfessorId(contexto.getProfessorId());
            alerta.setExperienciaId(contexto.getExperienciaId());
            alerta.setGrupoId(contexto.getGrupoId());
            alerta.setEstacaoId(contexto.getEstacaoId());
            alerta.setSensorId(contexto.getSensorId());
            alerta.setTipoSensor(contexto.getTipoSensor());
            alerta.setValorLido(contexto.getValorLido());
            alerta.setValorMin(regra.getValorMin());
            alerta.setValorMax(regra.getValorMax());
            alerta.setTitulo(regra.getTitulo());
            alerta.setMensagem(regra.getMensagem());
            alerta.setSeveridade(regra.getSeveridade());
            alerta.setEstado("NOVO");
            alerta.setCriadoEm(LocalDateTime.now());

            alertaRepository.save(alerta);
        }
    }


    private AlertaSensor obterAlerta(Long alertaId, Long utilizadorId, boolean admin) {
        if (admin) {
            return alertaRepository.findById(alertaId)
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND,
                            "Alerta não encontrado."
                    ));
        }

        return alertaRepository.findByIdAndProfessorId(alertaId, utilizadorId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Alerta não encontrado."
                ));
    }

    private void preencherRegra(
            RegraAlertaSensor regra,
            Long utilizadorId,
            RegraAlertaSensorFormDTO form
    ) {
        regra.setProfessorId(utilizadorId);
        regra.setGrupoId(form.getGrupoId());
        regra.setExperienciaId(form.getExperienciaId());
        regra.setEstacaoId(form.getEstacaoId());
        regra.setTipoSensor(form.getTipoSensor().trim().toUpperCase());
        regra.setOperador(form.getOperador().trim().toUpperCase());
        regra.setValorMin(form.getValorMin());
        regra.setValorMax(form.getValorMax());
        regra.setTitulo(form.getTitulo().trim());
        regra.setMensagem(normalizarTexto(form.getMensagem()));
        regra.setSeveridade(form.getSeveridade().trim().toUpperCase());
        regra.setAtivo(Boolean.TRUE.equals(form.getAtivo()));
        regra.setCooldownMinutos(form.getCooldownMinutos() != null ? form.getCooldownMinutos() : 10);
    }

    private void validarFormulario(RegraAlertaSensorFormDTO form, Long utilizadorId, boolean admin) {
        if (form.getTitulo() == null || form.getTitulo().trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O título é obrigatório.");
        }

        if (form.getTipoSensor() == null ||
                (!"TEMPERATURA".equalsIgnoreCase(form.getTipoSensor())
                        && !"TDS".equalsIgnoreCase(form.getTipoSensor())
                        && !"PH".equalsIgnoreCase(form.getTipoSensor()))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tipo de sensor inválido.");
        }

        if (form.getOperador() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O operador é obrigatório.");
        }

        String operador = form.getOperador().trim().toUpperCase();

        if (!List.of("ACIMA_DE", "ABAIXO_DE", "ENTRE", "FORA_INTERVALO").contains(operador)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Operador inválido.");
        }

        if ("ACIMA_DE".equals(operador) && form.getValorMax() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Para ACIMA_DE, define o valor máximo.");
        }

        if ("ABAIXO_DE".equals(operador) && form.getValorMin() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Para ABAIXO_DE, define o valor mínimo.");
        }

        if (("ENTRE".equals(operador) || "FORA_INTERVALO".equals(operador))
                && (form.getValorMin() == null || form.getValorMax() == null)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Para intervalos, define valor mínimo e máximo.");
        }

        if (form.getValorMin() != null && form.getValorMax() != null
                && form.getValorMin().compareTo(form.getValorMax()) > 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O valor mínimo não pode ser superior ao máximo.");
        }

        if (form.getGrupoId() != null) {
            boolean grupoValido = admin
                    ? grupoRepository.existsById(form.getGrupoId())
                    : grupoRepository.existsByIdAndProfessorId(form.getGrupoId(), utilizadorId);

            if (!grupoValido) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        admin ? "Grupo inválido." : "Grupo inválido para este professor."
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
                        admin ? "Experiência inválida." : "Experiência inválida para este professor."
                );
            }
        }

        if (form.getEstacaoId() != null) {
            boolean estacaoValida = admin
                    ? estacaoRepository.existsById(form.getEstacaoId())
                    : estacaoRepository.findEstacoesDoProfessor(utilizadorId)
                            .stream()
                            .anyMatch(estacao -> estacao.getId().equals(form.getEstacaoId()));

            if (!estacaoValida) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        admin ? "Estação inválida." : "Estação inválida para este professor."
                );
            }
        }

        if (form.getCooldownMinutos() != null && form.getCooldownMinutos() < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O cooldown não pode ser negativo.");
        }
    }

    private boolean regraDispara(RegraAlertaSensor regra, BigDecimal valor) {
        if (valor == null) {
            return false;
        }

        String operador = regra.getOperador();

        if ("ACIMA_DE".equals(operador)) {
            return regra.getValorMax() != null && valor.compareTo(regra.getValorMax()) > 0;
        }

        if ("ABAIXO_DE".equals(operador)) {
            return regra.getValorMin() != null && valor.compareTo(regra.getValorMin()) < 0;
        }

        if ("ENTRE".equals(operador)) {
            return regra.getValorMin() != null
                    && regra.getValorMax() != null
                    && valor.compareTo(regra.getValorMin()) >= 0
                    && valor.compareTo(regra.getValorMax()) <= 0;
        }

        if ("FORA_INTERVALO".equals(operador)) {
            return regra.getValorMin() != null
                    && regra.getValorMax() != null
                    && (valor.compareTo(regra.getValorMin()) < 0
                    || valor.compareTo(regra.getValorMax()) > 0);
        }

        return false;
    }

    private String normalizarTexto(String texto) {
        if (texto == null || texto.trim().isEmpty()) {
            return null;
        }

        return texto.trim();
    }
}