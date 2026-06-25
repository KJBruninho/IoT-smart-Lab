package com.iotroom.iotroom.dto;

public class AdminEstacaoForm {

    private Long id;
    private String nome;
    private String deviceId;
    private String localizacao;
    private boolean ativa = true;

    public static AdminEstacaoForm from(AdminEstacaoDTO dto) {
        AdminEstacaoForm form = new AdminEstacaoForm();
        form.setId(dto.id());
        form.setNome(dto.nome());
        form.setDeviceId(dto.deviceId());
        form.setLocalizacao(dto.localizacao());
        form.setAtiva(dto.ativa());
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

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId != null ? deviceId.trim() : null;
    }

    public String getLocalizacao() {
        return localizacao;
    }

    public void setLocalizacao(String localizacao) {
        this.localizacao = localizacao != null ? localizacao.trim() : null;
    }

    public boolean isAtiva() {
        return ativa;
    }

    public void setAtiva(boolean ativa) {
        this.ativa = ativa;
    }
}