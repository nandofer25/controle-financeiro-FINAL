package br.edu.fag.financeiro.cep.provedor;

/**
 * Origem do JSON bruto de um provedor: pode ser HTTP de verdade ou uma simulacao.
 * O Adapter nao sabe (nem precisa saber) qual das duas esta usando.
 */
@FunctionalInterface
public interface FonteJson {
    String consultar(String cep);
}
