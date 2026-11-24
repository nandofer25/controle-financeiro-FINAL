package br.edu.fag.financeiro.orcamento;

/** Orcamento inexistente. HTTP 404. */
public class OrcamentoNaoEncontradoException extends RuntimeException {
    public OrcamentoNaoEncontradoException(Long id) {
        super("Orcamento nao encontrado: id=" + id);
    }
}
