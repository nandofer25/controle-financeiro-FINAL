package br.edu.fag.financeiro.cep.adapter;

import br.edu.fag.financeiro.cep.CepNaoEncontradoException;
import br.edu.fag.financeiro.cep.ConsultaCep;
import br.edu.fag.financeiro.cep.Endereco;
import br.edu.fag.financeiro.cep.ProvedorCepException;
import br.edu.fag.financeiro.cep.provedor.FonteJson;
import br.edu.fag.financeiro.logger.AppLogger;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * ADAPTER da ViaCEP: adapta o JSON da ViaCEP
 * ({"cep":"01001-000","logradouro":..,"bairro":..,"localidade":..,"uf":..})
 * para a interface ConsultaCep do nosso sistema.
 */
public class ViaCepAdapter implements ConsultaCep {

    private final AppLogger log = AppLogger.getInstance();
    private final ObjectMapper json = new ObjectMapper();
    private final FonteJson fonte;

    public ViaCepAdapter(FonteJson fonte) {
        this.fonte = fonte;
    }

    @Override
    public Endereco buscar(String cep) {
        log.debug(getClass(), "[Adapter] consultando a ViaCEP");
        String corpo = fonte.consultar(cep);

        JsonNode no;
        try {
            no = json.readTree(corpo);
        } catch (JsonProcessingException e) {
            log.erro(getClass(), "[Adapter] resposta da ViaCEP ilegivel", e);
            throw new ProvedorCepException("Resposta invalida da ViaCEP", e);
        }
        if (no == null || !no.isObject()) {
            throw new ProvedorCepException("Resposta invalida da ViaCEP");
        }

        // ViaCEP devolve 200 com {"erro": true} (ou "true", em versoes antigas) se o CEP nao existe
        if ("true".equals(no.path("erro").asText())) {
            log.info(getClass(), "[Adapter] ViaCEP informou que o CEP nao existe");
            throw new CepNaoEncontradoException("CEP nao encontrado");
        }

        Endereco endereco = new Endereco(
                somenteDigitos(no.path("cep").asText("")),   // ViaCEP manda com hifen; padronizamos
                no.path("logradouro").asText(""),
                no.path("bairro").asText(""),
                no.path("localidade").asText(""),            // ViaCEP: localidade -> nosso: cidade
                no.path("uf").asText(""));                   // ViaCEP: uf         -> nosso: estado
        log.info(getClass(), "[Adapter] JSON da ViaCEP traduzido para Endereco");
        return endereco;
    }

    private static String somenteDigitos(String valor) {
        return valor.replaceAll("\\D", "");
    }
}
