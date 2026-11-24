package br.edu.fag.financeiro.cep;

/**
 * Interface do NOSSO sistema (alvo do Adapter e interface comum do Proxy).
 * O resto da aplicacao so conhece este tipo.
 */
public interface ConsultaCep {

    /**
     * @param cep 8 digitos, sem hifen (ex.: 01001000)
     * @throws CepInvalidoException       formato invalido
     * @throws CepNaoEncontradoException  provedor nao conhece o CEP
     * @throws ProvedorCepException       falha de comunicacao/resposta do provedor
     */
    Endereco buscar(String cep);
}
