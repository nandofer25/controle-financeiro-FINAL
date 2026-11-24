package br.edu.fag.financeiro.cep;

/** Provedor respondeu que o CEP nao existe. Vira HTTP 404. */
public class CepNaoEncontradoException extends RuntimeException {
    public CepNaoEncontradoException(String mensagem) {
        super(mensagem);
    }
}
