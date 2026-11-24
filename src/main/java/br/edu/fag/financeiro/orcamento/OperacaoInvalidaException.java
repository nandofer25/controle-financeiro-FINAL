package br.edu.fag.financeiro.orcamento;

/** Operacao incompativel com o estado atual do orcamento (ex.: reabrir um orcamento que ja esta aberto). HTTP 409. */
public class OperacaoInvalidaException extends RuntimeException {
    public OperacaoInvalidaException(String mensagem) {
        super(mensagem);
    }
}
