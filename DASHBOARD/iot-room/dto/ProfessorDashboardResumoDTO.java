package com.iotroom.iotroom.dto;

import java.time.LocalDateTime;
import java.util.List;

public class ProfessorDashboardResumoDTO {

    private long estacoesOnline;
    private long sensoresAtivos;
    private long alertasNovos;
    private LocalDateTime ultimaLeituraEm;
    private String ultimaLeituraTexto;

    private List<DashboardLeituraDTO> ultimasLeituras;
    private List<DashboardAlertaDTO> alertasRecentes;
    private List<DashboardExperienciaDTO> experienciasAtivas;
    private List<DashboardEstacaoStatusDTO> estadoEstacoes;

    public ProfessorDashboardResumoDTO(
            long estacoesOnline,
            long sensoresAtivos,
            long alertasNovos,
            LocalDateTime ultimaLeituraEm,
            String ultimaLeituraTexto,
            List<DashboardLeituraDTO> ultimasLeituras,
            List<DashboardAlertaDTO> alertasRecentes,
            List<DashboardExperienciaDTO> experienciasAtivas,
            List<DashboardEstacaoStatusDTO> estadoEstacoes
    ) {
        this.estacoesOnline = estacoesOnline;
        this.sensoresAtivos = sensoresAtivos;
        this.alertasNovos = alertasNovos;
        this.ultimaLeituraEm = ultimaLeituraEm;
        this.ultimaLeituraTexto = ultimaLeituraTexto;
        this.ultimasLeituras = ultimasLeituras;
        this.alertasRecentes = alertasRecentes;
        this.experienciasAtivas = experienciasAtivas;
        this.estadoEstacoes = estadoEstacoes;
    }

    public long getEstacoesOnline() { return estacoesOnline; }
    public long getSensoresAtivos() { return sensoresAtivos; }
    public long getAlertasNovos() { return alertasNovos; }
    public LocalDateTime getUltimaLeituraEm() { return ultimaLeituraEm; }
    public String getUltimaLeituraTexto() { return ultimaLeituraTexto; }
    public List<DashboardLeituraDTO> getUltimasLeituras() { return ultimasLeituras; }
    public List<DashboardAlertaDTO> getAlertasRecentes() { return alertasRecentes; }
    public List<DashboardExperienciaDTO> getExperienciasAtivas() { return experienciasAtivas; }
    public List<DashboardEstacaoStatusDTO> getEstadoEstacoes() { return estadoEstacoes; }
}