package br.edu.fag.financeiro.cep.provedor;

import br.edu.fag.financeiro.cep.CepNaoEncontradoException;

import java.util.Map;

/** Simula a BrasilAPI offline: JSON fixo NO FORMATO DA BRASILAPI (street, city, state...). */
public class BrasilApiSimulado implements FonteJson {

    private static final Map<String, String> BASE = Map.of(
            "01001000", """
                    {"cep":"01001000","state":"SP","city":"São Paulo","neighborhood":"Sé",
                     "street":"Praça da Sé","service":"viacep"}""",
            "89010025", """
                    {"cep":"89010025","state":"SC","city":"Blumenau","neighborhood":"Centro",
                     "street":"Rua Doutor Luiz de Freitas Melro","service":"viacep"}"""
    );

    @Override
    public String consultar(String cep) {
        String json = BASE.get(cep);
        if (json == null) {
            // a BrasilAPI responde HTTP 404 quando o CEP nao existe
            throw new CepNaoEncontradoException("CEP nao encontrado no provedor");
        }
        return json;
    }
}
