package br.edu.fag.financeiro.orcamento;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/** Alerta gerado quando um orcamento ENTRA em ATENCAO ou ESTOURADO (so na transicao, para nao repetir aviso a cada lancamento). */
@Entity
@Table(name = "alertas_orcamento")
public class AlertaOrcamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long orcamentoId;

    @Column(nullable = false)
    private String categoria;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusOrcamento status;

    @Column(nullable = false, length = 500)
    private String mensagem;

    @Column(nullable = false)
    private LocalDateTime criadoEm;

    protected AlertaOrcamento() {
    }

    public AlertaOrcamento(Long orcamentoId, String categoria, StatusOrcamento status, String mensagem) {
        this.orcamentoId = orcamentoId;
        this.categoria = categoria;
        this.status = status;
        this.mensagem = mensagem;
        this.criadoEm = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public Long getOrcamentoId() { return orcamentoId; }
    public String getCategoria() { return categoria; }
    public StatusOrcamento getStatus() { return status; }
    public String getMensagem() { return mensagem; }
    public LocalDateTime getCriadoEm() { return criadoEm; }
}
