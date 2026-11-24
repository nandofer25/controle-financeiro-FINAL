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
 * ADAPTER da BrasilAPI: adapta o JSON da BrasilAPI
 * ({"cep":"01001000","street":..,"neighborhood":..,"city":..,"state":..})
 * para a interface ConsultaCep do nosso sistema.
 */
public class BrasilApiAdapter implements ConsultaCep {

    private final AppLogger log = AppLogger.getInstance();
    private final ObjectMapper json = new ObjectMapper();
    private final FonteJson fonte;

    public BrasilApiAdapter(FonteJson fonte) {
        this.fonte = fonte;
    }

    @Override
    public Endereco buscar(String cep) {
        log.debug(getClass(), "[Adapter] consultando a BrasilAPI");
        String corpo = fonte.consultar(cep);   // CEP inexistente = HTTP 404 (a fonte ja lanca a excecao)

        JsonNode no;
        try {
            no = json.readTree(corpo);
        } catch (JsonProcessingException e) {
            log.erro(getClass(), "[Adapter] resposta da BrasilAPI ilegivel", e);
            throw new ProvedorCepException("Resposta invalida da BrasilAPI", e);
        }
        if (no == null || !no.isObject()) {
            throw new ProvedorCepException("Resposta invalida da BrasilAPI");
        }
        if (!no.hasNonNull("cep")) {
            log.info(getClass(), "[Adapter] BrasilAPI respondeu sem CEP (nao encontrado)");
            throw new CepNaoEncontradoException("CEP nao encontrado");
        }

        Endereco endereco = new Endereco(
                no.path("cep").asText("").replaceAll("\\D", ""),
                no.path("street").asText(""),          // BrasilAPI: street       -> nosso: logradouro
                no.path("neighborhood").asText(""),    // BrasilAPI: neighborhood -> nosso: bairro
                no.path("city").asText(""),            // BrasilAPI: city         -> nosso: cidade
                no.path("state").asText(""));          // BrasilAPI: state        -> nosso: estado
        log.info(getClass(), "[Adapter] JSON da BrasilAPI traduzido para Endereco");
        return endereco;
    }
}
