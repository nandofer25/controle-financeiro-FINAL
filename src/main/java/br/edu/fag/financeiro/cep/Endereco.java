package br.edu.fag.financeiro.cep;

/**
 * Objeto do NOSSO sistema. Os nomes dos campos sao nossos e nao dependem de nenhum provedor
 * (ViaCEP usa "localidade"/"uf", BrasilAPI usa "city"/"state"; aqui e sempre cidade/estado).
 */
public record Endereco(String cep, String logradouro, String bairro, String cidade, String estado) {
}
