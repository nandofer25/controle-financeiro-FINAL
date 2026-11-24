package br.edu.fag.financeiro.cep.provedor;

import java.util.Map;

/** Simula a ViaCEP offline: devolve JSON fixo NO FORMATO DA VIACEP (localidade, uf, "erro"). */
public class ViaCepSimulado implements FonteJson {

    private static final Map<String, String> BASE = Map.of(
            "01001000", """
                    {"cep":"01001-000","logradouro":"Praça da Sé","complemento":"lado ímpar",
                     "bairro":"Sé","localidade":"São Paulo","uf":"SP","ibge":"3550308",
                     "gia":"1004","ddd":"11","siafi":"7107"}""",
            "89010025", """
                    {"cep":"89010-025","logradouro":"Rua Doutor Luiz de Freitas Melro","complemento":"",
                     "bairro":"Centro","localidade":"Blumenau","uf":"SC","ibge":"4202404",
                     "gia":"","ddd":"47","siafi":"8179"}"""
    );

    @Override
    public String consultar(String cep) {
        // a ViaCEP responde 200 com {"erro": true} quando o CEP nao existe
        return BASE.getOrDefault(cep, "{\"erro\": true}");
    }
}
