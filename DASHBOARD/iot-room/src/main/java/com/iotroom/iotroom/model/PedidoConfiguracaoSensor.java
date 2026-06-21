package com.iotroom.iotroom.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "pedidos_configuracao_sensor")
public class PedidoConfiguracaoSensor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "sensor_id", nullable = false)
    private Long sensorId;

    @Column(name = "solicitado_por")
    private Long solicitadoPor;

    @Column(name = "analisado_por")
    private Long analisadoPor;

    @Column(nullable = false)
    private String origem = "RESEARCHER";

    @Column(nullable = false)
    private String estado = "PENDENTE";

    @Column(name = "intervalo_rapido_ms", nullable = false)
    private Integer intervaloRapidoMs;

    @Column(name = "intervalo_estavel_ms", nullable = false)
    private Integer intervaloEstavelMs;

    @Column(name = "duracao_modo_rapido_ms", nullable = false)
    private Integer duracaoModoRapidoMs;

    @Column(name = "delta_significativo", nullable = false)
    private BigDecimal deltaSignificativo;

    @Column(columnDefinition = "TEXT")
    private String motivo;

    @Column(name = "resposta_professor", columnDefinition = "TEXT")
    private String respostaProfessor;

    @Column(name = "comando_id")
    private Long comandoId;

    @Column(name = "criado_em")
    private LocalDateTime criadoEm;

    @Column(name = "analisado_em")
    private LocalDateTime analisadoEm;

    @Column(name = "aplicado_em")
    private LocalDateTime aplicadoEm;

    @PrePersist
    public void prePersist() {
        if (criadoEm == null) criadoEm = LocalDateTime.now();
        if (estado == null) estado = "PENDENTE";
        if (origem == null) origem = "RESEARCHER";
    }

    public Long getId() { return id; }
    public Long getSensorId() { return sensorId; }
    public Long getSolicitadoPor() { return solicitadoPor; }
    public Long getAnalisadoPor() { return analisadoPor; }
    public String getOrigem() { return origem; }
    public String getEstado() { return estado; }
    public Integer getIntervaloRapidoMs() { return intervaloRapidoMs; }
    public Integer getIntervaloEstavelMs() { return intervaloEstavelMs; }
    public Integer getDuracaoModoRapidoMs() { return duracaoModoRapidoMs; }
    public BigDecimal getDeltaSignificativo() { return deltaSignificativo; }
    public String getMotivo() { return motivo; }
    public String getRespostaProfessor() { return respostaProfessor; }
    public Long getComandoId() { return comandoId; }
    public LocalDateTime getCriadoEm() { return criadoEm; }
    public LocalDateTime getAnalisadoEm() { return analisadoEm; }
    public LocalDateTime getAplicadoEm() { return aplicadoEm; }

    public void setId(Long id) { this.id = id; }
    public void setSensorId(Long sensorId) { this.sensorId = sensorId; }
    public void setSolicitadoPor(Long solicitadoPor) { this.solicitadoPor = solicitadoPor; }
    public void setAnalisadoPor(Long analisadoPor) { this.analisadoPor = analisadoPor; }
    public void setOrigem(String origem) { this.origem = origem; }
    public void setEstado(String estado) { this.estado = estado; }
    public void setIntervaloRapidoMs(Integer intervaloRapidoMs) { this.intervaloRapidoMs = intervaloRapidoMs; }
    public void setIntervaloEstavelMs(Integer intervaloEstavelMs) { this.intervaloEstavelMs = intervaloEstavelMs; }
    public void setDuracaoModoRapidoMs(Integer duracaoModoRapidoMs) { this.duracaoModoRapidoMs = duracaoModoRapidoMs; }
    public void setDeltaSignificativo(BigDecimal deltaSignificativo) { this.deltaSignificativo = deltaSignificativo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }
    public void setRespostaProfessor(String respostaProfessor) { this.respostaProfessor = respostaProfessor; }
    public void setComandoId(Long comandoId) { this.comandoId = comandoId; }
    public void setCriadoEm(LocalDateTime criadoEm) { this.criadoEm = criadoEm; }
    public void setAnalisadoEm(LocalDateTime analisadoEm) { this.analisadoEm = analisadoEm; }
    public void setAplicadoEm(LocalDateTime aplicadoEm) { this.aplicadoEm = aplicadoEm; }
}