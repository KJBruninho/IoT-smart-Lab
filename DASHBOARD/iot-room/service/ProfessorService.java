package com.iotroom.iotroom.service;

import com.iotroom.iotroom.dto.ProfessorDashboardDTO;
import com.iotroom.iotroom.model.Aviso;
import com.iotroom.iotroom.model.Experiencia;
import com.iotroom.iotroom.model.Grupo;
import com.iotroom.iotroom.repository.AvisoRepository;
import com.iotroom.iotroom.repository.ExperienciaRepository;
import com.iotroom.iotroom.repository.GrupoRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProfessorService {

    private final GrupoRepository grupoRepository;
    private final ExperienciaRepository experienciaRepository;
    private final AvisoRepository avisoRepository;

    public ProfessorService(
            GrupoRepository grupoRepository,
            ExperienciaRepository experienciaRepository,
            AvisoRepository avisoRepository
    ) {
        this.grupoRepository = grupoRepository;
        this.experienciaRepository = experienciaRepository;
        this.avisoRepository = avisoRepository;
    }

    public ProfessorDashboardDTO obterDashboard(Long professorId) {

        long totalGrupos = grupoRepository.countByProfessorIdAndAtivoTrue(professorId);

        long experienciasAtivas = experienciaRepository.countByCriadoPorIdAndEstadoIn(
                professorId,
                List.of("AGENDADA", "ATIVA", "PAUSADA")
        );

        long experienciasConcluidas = experienciaRepository.countByCriadoPorIdAndEstadoIn(
                professorId,
                List.of("CONCLUIDA", "SANADA")
        );

        long avisosAtivos = avisoRepository.countByCriadoPorIdAndAtivoTrue(professorId);

        return new ProfessorDashboardDTO(
                totalGrupos,
                experienciasAtivas,
                experienciasConcluidas,
                avisosAtivos
        );
    }

    public List<Grupo> obterGruposDoProfessor(Long professorId) {
        return grupoRepository.findByProfessorIdOrderByCriadoEmDesc(professorId);
    }

    public List<Grupo> obterUltimosGruposDoProfessor(Long professorId) {
        return grupoRepository.findTop5ByProfessorIdOrderByCriadoEmDesc(professorId);
    }

    public List<Experiencia> obterExperienciasDoProfessor(Long professorId) {
        return experienciaRepository.findByCriadoPorIdOrderByCriadoEmDesc(professorId);
    }

    public List<Experiencia> obterUltimasExperienciasDoProfessor(Long professorId) {
        return experienciaRepository.findTop5ByCriadoPorIdOrderByCriadoEmDesc(professorId);
    }

    public List<Aviso> obterAvisosDoProfessor(Long professorId) {
        return avisoRepository.findByCriadoPorIdOrderByCriadoEmDesc(professorId);
    }

    public List<Aviso> obterUltimosAvisosDoProfessor(Long professorId) {
        return avisoRepository.findTop5ByCriadoPorIdOrderByCriadoEmDesc(professorId);
    }
}