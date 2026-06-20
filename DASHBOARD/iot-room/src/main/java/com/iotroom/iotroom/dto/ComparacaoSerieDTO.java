package com.iotroom.iotroom.dto;

import java.util.List;

public class ComparacaoSerieDTO {

    private String nome;
    private ComparacaoFiltroDTO filtro;
    private List<ComparacaoLeituraDTO> leituras;

    public ComparacaoSerieDTO() {
    }

    public ComparacaoSerieDTO(
            String nome,
            ComparacaoFiltroDTO filtro,
            List<ComparacaoLeituraDTO> leituras
    ) {
        this.nome = nome;
        this.filtro = filtro;
        this.leituras = leituras;
    }

    public String getNome() { return nome; }
    public ComparacaoFiltroDTO getFiltro() { return filtro; }
    public List<ComparacaoLeituraDTO> getLeituras() { return leituras; }

    public void setNome(String nome) { this.nome = nome; }
    public void setFiltro(ComparacaoFiltroDTO filtro) { this.filtro = filtro; }
    public void setLeituras(List<ComparacaoLeituraDTO> leituras) { this.leituras = leituras; }
}