package com.iotroom.iotroom.dto.dashboard;

import java.time.LocalDateTime;

public class DashboardExperienciaDTO {

    private Long experienciaId;
    private String nome;
    private String grupoNome;
    private String estado;
    private LocalDateTime ultimaLeituraEm;
    private String ultimaLeituraTexto;
    private Long totalLeituras;

    public DashboardExperienciaDTO(DashboardExperienciaProjection projection, String ultimaLeituraTexto) {
        this.experienciaId = projection.getExperienciaId();
        this.nome = projection.getNome();
        this.grupoNome = projection.getGrupoNome();
        this.estado = projection.getEstado();
        this.totalLeituras = projection.getTotalLeituras();
        this.ultimaLeituraTexto = ultimaLeituraTexto;

        if (projection.getUltimaLeituraEm() != null) {
            this.ultimaLeituraEm = projection.getUltimaLeituraEm().toLocalDateTime();
        }
    }

    public Long getExperienciaId() { return experienciaId; }
    public String getNome() { return nome; }
    public String getGrupoNome() { return grupoNome; }
    public String getEstado() { return estado; }
    public LocalDateTime getUltimaLeituraEm() { return ultimaLeituraEm; }
    public String getUltimaLeituraTexto() { return ultimaLeituraTexto; }
    public Long getTotalLeituras() { return totalLeituras; }
}