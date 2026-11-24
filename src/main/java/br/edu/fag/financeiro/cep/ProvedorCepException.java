package br.edu.fag.financeiro.cep;

/** Provedor fora do ar, timeout ou resposta ilegivel. Vira HTTP 502. */
public class ProvedorCepException extends RuntimeException {
    public ProvedorCepException(String mensagem) {
        super(mensagem);
    }

    public ProvedorCepException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }
}
