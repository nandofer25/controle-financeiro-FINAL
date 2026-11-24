package br.edu.fag.financeiro.cep;

/** CEP fora do formato esperado (8 digitos). Vira HTTP 400. */
public class CepInvalidoException extends RuntimeException {
    public CepInvalidoException(String mensagem) {
        super(mensagem);
    }
}
