package br.edu.fag.financeiro.orcamento;

/** Como o limite de um orcamento e definido. Cada valor tem uma Strategy correspondente. */
public enum TipoLimite {
    /** valorLimite e um valor em reais (ex.: 800.00). */
    FIXO,
    /** valorLimite e um percentual das receitas do mes (ex.: 30 = 30%). */
    PERCENTUAL_DA_RECEITA
}
