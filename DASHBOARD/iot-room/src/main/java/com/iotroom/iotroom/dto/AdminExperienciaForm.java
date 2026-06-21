package com.iotroom.iotroom.dto;

import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class AdminExperienciaForm {

    private Long id;
    private String nome;
    private String descricao;
    private Long grupoId;

    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime dataInicio = LocalDateTime.now();

    private List<Long> estacaoIds = new ArrayList<>();

    public static AdminExperienciaForm from(AdminExperienciaDTO dto, List<Long> estacaoIds) {
        AdminExperienciaForm form = new AdminExperienciaForm();
        form.setId(dto.id());
        form.setNome(dto.nome());
        form.setDescricao(dto.descricao());
        form.setGrupoId(dto.grupoId());
        form.setDataInicio(dto.dataInicio());
        form.setEstacaoIds(estacaoIds);
        return form;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome != null ? nome.trim() : null;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao != null ? descricao.trim() : null;
    }

    public Long getGrupoId() {
        return grupoId;
    }

    public void setGrupoId(Long grupoId) {
        this.grupoId = grupoId;
    }

    public LocalDateTime getDataInicio() {
        return dataInicio;
    }

    public void setDataInicio(LocalDateTime dataInicio) {
        this.dataInicio = dataInicio;
    }

    public List<Long> getEstacaoIds() {
        return estacaoIds;
    }

    public void setEstacaoIds(List<Long> estacaoIds) {
        this.estacaoIds = estacaoIds != null ? estacaoIds : new ArrayList<>();
    }
}