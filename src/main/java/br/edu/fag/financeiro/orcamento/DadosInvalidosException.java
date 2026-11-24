package br.edu.fag.financeiro.orcamento;

/** Dado que passou na validacao de formato mas viola uma regra de negocio (ex.: percentual acima de 100). HTTP 400. */
public class DadosInvalidosException extends RuntimeException {
    public DadosInvalidosException(String mensagem) {
        super(mensagem);
    }
}
