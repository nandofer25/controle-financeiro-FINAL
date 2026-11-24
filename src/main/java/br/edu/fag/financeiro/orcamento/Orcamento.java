package br.edu.fag.financeiro.orcamento;

import br.edu.fag.financeiro.orcamento.estado.EstadoOrcamento;
import br.edu.fag.financeiro.orcamento.estado.EstadoOrcamentoFactory;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * Orcamento mensal de UMA categoria. E o CONTEXTO do padrao State: guarda so o status
 * (enum persistido) e delega as operacoes ao objeto de estado correspondente.
 */
@Entity
@Table(name = "orcamentos", uniqueConstraints = @UniqueConstraint(columnNames = {"categoria", "ano", "mes"}))
public class Orcamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String categoria;

    @Column(nullable = false)
    private int ano;

    @Column(nullable = false)
    private int mes;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoLimite tipoLimite;

    /** Reais (FIXO) ou percentual (PERCENTUAL_DA_RECEITA), conforme tipoLimite. */
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal valorLimite;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusOrcamento status = StatusOrcamento.NORMAL;

    protected Orcamento() {
    }

    public Orcamento(String categoria, int ano, int mes, TipoLimite tipoLimite, BigDecimal valorLimite) {
        this.categoria = categoria;
        this.ano = ano;
        this.mes = mes;
        this.tipoLimite = tipoLimite;
        this.valorLimite = valorLimite;
    }

    // ---- operacoes de dominio: delegam ao ESTADO atual ----

    public void atualizarConsumo(BigDecimal consumido, BigDecimal limite) {
        estado().atualizarConsumo(this, consumido, limite);
    }

    public void encerrar() {
        estado().encerrar(this);
    }

    public void reabrir() {
        estado().reabrir(this);
    }

    public Optional<String> mensagemDeEntrada(BigDecimal consumido, BigDecimal limite) {
        return estado().mensagemDeEntrada(this, consumido, limite);
    }

    /** Usado pelos estados para efetuar a transicao. */
    public void mudarPara(StatusOrcamento novo) {
        this.status = novo;
    }

    private EstadoOrcamento estado() {
        return EstadoOrcamentoFactory.de(status);
    }

    // ---- limite: editavel apenas se o estado permitir ----

    public void redefinirLimite(TipoLimite tipoLimite, BigDecimal valorLimite) {
        estado().aoAlterarLimite(this); // o estado pode vetar (encerrado)
        this.tipoLimite = tipoLimite;
        this.valorLimite = valorLimite;
    }

    public Long getId() { return id; }
    public String getCategoria() { return categoria; }
    public int getAno() { return ano; }
    public int getMes() { return mes; }
    public TipoLimite getTipoLimite() { return tipoLimite; }
    public BigDecimal getValorLimite() { return valorLimite; }
    public StatusOrcamento getStatus() { return status; }
}
