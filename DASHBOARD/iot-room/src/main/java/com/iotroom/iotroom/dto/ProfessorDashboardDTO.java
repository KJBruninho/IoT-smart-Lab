package com.iotroom.iotroom.dto;

public class ProfessorDashboardDTO {

    private long totalGrupos;
    private long experienciasAtivas;
    private long experienciasConcluidas;
    private long avisosAtivos;

    public ProfessorDashboardDTO() {
    }

    public ProfessorDashboardDTO(
            long totalGrupos,
            long experienciasAtivas,
            long experienciasConcluidas,
            long avisosAtivos
    ) {
        this.totalGrupos = totalGrupos;
        this.experienciasAtivas = experienciasAtivas;
        this.experienciasConcluidas = experienciasConcluidas;
        this.avisosAtivos = avisosAtivos;
    }

    public long getTotalGrupos() {
        return totalGrupos;
    }

    public void setTotalGrupos(long totalGrupos) {
        this.totalGrupos = totalGrupos;
    }

    public long getExperienciasAtivas() {
        return experienciasAtivas;
    }

    public void setExperienciasAtivas(long experienciasAtivas) {
        this.experienciasAtivas = experienciasAtivas;
    }

    public long getExperienciasConcluidas() {
        return experienciasConcluidas;
    }

    public void setExperienciasConcluidas(long experienciasConcluidas) {
        this.experienciasConcluidas = experienciasConcluidas;
    }

    public long getAvisosAtivos() {
        return avisosAtivos;
    }

    public void setAvisosAtivos(long avisosAtivos) {
        this.avisosAtivos = avisosAtivos;
    }
}