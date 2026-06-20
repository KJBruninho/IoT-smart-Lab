package com.iotroom.iotroom.service;

import com.iotroom.iotroom.dto.*;
import com.iotroom.iotroom.repository.ProfessorDashboardRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class ProfessorDashboardService {

    private final ProfessorDashboardRepository professorDashboardRepository;

    public ProfessorDashboardService(ProfessorDashboardRepository professorDashboardRepository) {
        this.professorDashboardRepository = professorDashboardRepository;
    }

    public ProfessorDashboardResumoDTO obterDashboard(Long professorId) {
        long sensoresAtivos = professorDashboardRepository.countSensoresAtivos(professorId);
        long estacoesOnline = professorDashboardRepository.countEstacoesOnline(professorId);
        long alertasNovos = professorDashboardRepository.countAlertasNovos(professorId);

        LocalDateTime ultimaLeituraEm = converterTimestamp(
                professorDashboardRepository.findUltimaLeituraEm(professorId)
        );

        List<DashboardLeituraDTO> ultimasLeituras = obterUltimasLeituras(professorId);
        List<DashboardAlertaDTO> alertasRecentes = obterAlertasRecentes(professorId);
        List<DashboardExperienciaDTO> experienciasAtivas = obterExperienciasAtivas(professorId);
        List<DashboardEstacaoStatusDTO> estadoEstacoes = obterEstadoEstacoes(professorId);

        return new ProfessorDashboardResumoDTO(
                estacoesOnline,
                sensoresAtivos,
                alertasNovos,
                ultimaLeituraEm,
                tempoRelativo(ultimaLeituraEm),
                ultimasLeituras,
                alertasRecentes,
                experienciasAtivas,
                estadoEstacoes
        );
    }

    private List<DashboardLeituraDTO> obterUltimasLeituras(Long professorId) {
        List<DashboardLeituraProjection> projections =
                professorDashboardRepository.findUltimasLeituras(
                        professorId,
                        PageRequest.of(0, 20)
                );

        List<DashboardLeituraDTO> resultado = new ArrayList<>();

        for (DashboardLeituraProjection projection : projections) {
            LocalDateTime data = converterTimestamp(projection.getDataRegisto());
            resultado.add(new DashboardLeituraDTO(projection, tempoRelativo(data)));
        }

        return resultado;
    }

    private List<DashboardAlertaDTO> obterAlertasRecentes(Long professorId) {
        List<DashboardAlertaProjection> projections =
                professorDashboardRepository.findAlertasRecentes(
                        professorId,
                        PageRequest.of(0, 5)
                );

        List<DashboardAlertaDTO> resultado = new ArrayList<>();

        for (DashboardAlertaProjection projection : projections) {
            LocalDateTime data = converterTimestamp(projection.getCriadoEm());
            resultado.add(new DashboardAlertaDTO(projection, tempoRelativo(data)));
        }

        return resultado;
    }

    private List<DashboardExperienciaDTO> obterExperienciasAtivas(Long professorId) {
        List<DashboardExperienciaProjection> projections =
                professorDashboardRepository.findExperienciasAtivas(
                        professorId,
                        PageRequest.of(0, 6)
                );

        List<DashboardExperienciaDTO> resultado = new ArrayList<>();

        for (DashboardExperienciaProjection projection : projections) {
            LocalDateTime data = converterTimestamp(projection.getUltimaLeituraEm());
            resultado.add(new DashboardExperienciaDTO(projection, tempoRelativo(data)));
        }

        return resultado;
    }

    private List<DashboardEstacaoStatusDTO> obterEstadoEstacoes(Long professorId) {
        List<DashboardEstacaoProjection> projections =
                professorDashboardRepository.findEstadoEstacoes(
                        professorId,
                        PageRequest.of(0, 8)
                );

        List<DashboardEstacaoStatusDTO> resultado = new ArrayList<>();

        for (DashboardEstacaoProjection projection : projections) {
            LocalDateTime ultima = converterTimestamp(projection.getUltimaLeituraEm());

            String estado = calcularEstadoEstacao(ultima);
            String estadoCss = calcularEstadoCss(estado);

            resultado.add(new DashboardEstacaoStatusDTO(
                    projection,
                    tempoRelativo(ultima),
                    estado,
                    estadoCss
            ));
        }

        return resultado;
    }

    private String calcularEstadoEstacao(LocalDateTime ultimaLeitura) {
        if (ultimaLeitura == null) {
            return "SEM DADOS";
        }

        long minutos = Duration.between(ultimaLeitura, LocalDateTime.now()).toMinutes();

        if (minutos < 2) {
            return "ONLINE";
        }

        if (minutos < 10) {
            return "ATRASADA";
        }

        return "OFFLINE";
    }

    private String calcularEstadoCss(String estado) {
        return switch (estado) {
            case "ONLINE" -> "status-active";
            case "ATRASADA" -> "status-created";
            case "OFFLINE" -> "status-inactive";
            default -> "status-neutral";
        };
    }

    private LocalDateTime converterTimestamp(Timestamp timestamp) {
        return timestamp != null ? timestamp.toLocalDateTime() : null;
    }

    private String tempoRelativo(LocalDateTime data) {
        if (data == null) {
            return "sem dados";
        }

        Duration duration = Duration.between(data, LocalDateTime.now());

        long segundos = duration.toSeconds();
        long minutos = duration.toMinutes();
        long horas = duration.toHours();
        long dias = duration.toDays();

        if (segundos < 10) {
            return "agora";
        }

        if (segundos < 60) {
            return "há " + segundos + "s";
        }

        if (minutos < 60) {
            return "há " + minutos + " min";
        }

        if (horas < 24) {
            return "há " + horas + "h";
        }

        return "há " + dias + "d";
    }
}