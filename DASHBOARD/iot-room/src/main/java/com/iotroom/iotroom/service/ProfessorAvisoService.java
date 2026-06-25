package com.iotroom.iotroom.service;

import com.iotroom.iotroom.dto.AvisoFormDTO;
import com.iotroom.iotroom.model.Aviso;
import com.iotroom.iotroom.repository.AvisoRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;

@Service
public class ProfessorAvisoService {

    private final AvisoRepository avisoRepository;

    public ProfessorAvisoService(AvisoRepository avisoRepository) {
        this.avisoRepository = avisoRepository;
    }

    public List<Aviso> listarAvisosDoProfessor(Long professorId) {
        return avisoRepository.findByCriadoPorIdOrderByCriadoEmDesc(professorId);
    }

    public Aviso obterAvisoDoProfessor(Long avisoId, Long professorId) {
        return avisoRepository.findByIdAndCriadoPorId(avisoId, professorId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Aviso não encontrado para este professor."
                ));
    }

    @Transactional
    public Aviso criarAviso(Long professorId, AvisoFormDTO form) {
        validarFormulario(form);

        Aviso aviso = new Aviso();
        aviso.setTitulo(form.getTitulo().trim());
        aviso.setMensagem(form.getMensagem().trim());
        aviso.setCriadoPorId(professorId);
        aviso.setAtivo(true);
        aviso.setCriadoEm(LocalDateTime.now());
        aviso.setExpiraEm(converterExpiraEm(form.getExpiraEm()));

        return avisoRepository.save(aviso);
    }

    @Transactional
    public Aviso atualizarAviso(Long avisoId, Long professorId, AvisoFormDTO form) {
        validarFormulario(form);

        Aviso aviso = obterAvisoDoProfessor(avisoId, professorId);

        aviso.setTitulo(form.getTitulo().trim());
        aviso.setMensagem(form.getMensagem().trim());
        aviso.setAtivo(Boolean.TRUE.equals(form.getAtivo()));
        aviso.setExpiraEm(converterExpiraEm(form.getExpiraEm()));

        return avisoRepository.save(aviso);
    }

    @Transactional
    public void alternarEstado(Long avisoId, Long professorId) {
        Aviso aviso = obterAvisoDoProfessor(avisoId, professorId);

        boolean ativoAtual = Boolean.TRUE.equals(aviso.getAtivo());
        aviso.setAtivo(!ativoAtual);

        avisoRepository.save(aviso);
    }

    public AvisoFormDTO criarFormVazio() {
        return new AvisoFormDTO(null, "", "", true, null);
    }

    public AvisoFormDTO criarFormAPartirDeAviso(Aviso aviso) {
        return new AvisoFormDTO(
                aviso.getId(),
                aviso.getTitulo(),
                aviso.getMensagem(),
                aviso.getAtivo(),
                aviso.getExpiraEm() != null ? aviso.getExpiraEm().toString() : null
        );
    }

    private void validarFormulario(AvisoFormDTO form) {
        if (form.getTitulo() == null || form.getTitulo().trim().isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "O título do aviso é obrigatório."
            );
        }

        if (form.getTitulo().trim().length() > 150) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "O título do aviso não pode ter mais de 150 caracteres."
            );
        }

        if (form.getMensagem() == null || form.getMensagem().trim().isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "A mensagem do aviso é obrigatória."
            );
        }
    }

    private LocalDateTime converterExpiraEm(String valor) {
        if (valor == null || valor.trim().isEmpty()) {
            return null;
        }

        try {
            return LocalDateTime.parse(valor.trim());
        } catch (DateTimeParseException e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "A data de expiração é inválida."
            );
        }
    }
}