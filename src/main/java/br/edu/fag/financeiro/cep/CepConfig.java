package br.edu.fag.financeiro.cep;

import br.edu.fag.financeiro.cep.adapter.BrasilApiAdapter;
import br.edu.fag.financeiro.cep.adapter.ViaCepAdapter;
import br.edu.fag.financeiro.cep.provedor.BrasilApiSimulado;
import br.edu.fag.financeiro.cep.provedor.FonteJson;
import br.edu.fag.financeiro.cep.provedor.HttpFonteJson;
import br.edu.fag.financeiro.cep.provedor.ViaCepSimulado;
import br.edu.fag.financeiro.logger.AppLogger;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * Unico lugar que sabe qual provedor esta ativo. Quem decide e a propriedade
 * "cep.provedor" (viacep | brasilapi) no application.properties.
 * "cep.modo" escolhe entre "real" (HTTP) e "simulado" (JSON fixo, sem internet).
 *
 * Montagem final: Controller -> ConsultaCepProxy -> Adapter do provedor -> fonte de JSON
 */
@Configuration
public class CepConfig {

    private static final String URL_VIACEP = "https://viacep.com.br/ws/%s/json/";
    private static final String URL_BRASILAPI = "https://brasilapi.com.br/api/cep/v1/%s";

    // ---- provedor REAL (so um dos dois e criado, conforme cep.provedor) ----

    @Bean("provedorCep")
    @ConditionalOnProperty(name = "cep.provedor", havingValue = "viacep", matchIfMissing = true)
    public ConsultaCep viaCep(@Value("${cep.modo:real}") String modo) {
        AppLogger.getInstance().info(CepConfig.class, "Provedor de CEP ativo: ViaCEP (modo " + modo + ")");
        return new ViaCepAdapter(fonte(modo, new ViaCepSimulado(), URL_VIACEP));
    }

    @Bean("provedorCep")
    @ConditionalOnProperty(name = "cep.provedor", havingValue = "brasilapi")
    public ConsultaCep brasilApi(@Value("${cep.modo:real}") String modo) {
        AppLogger.getInstance().info(CepConfig.class, "Provedor de CEP ativo: BrasilAPI (modo " + modo + ")");
        return new BrasilApiAdapter(fonte(modo, new BrasilApiSimulado(), URL_BRASILAPI));
    }

    // ---- PROXY: e o bean "principal" do tipo ConsultaCep; e ele que o Controller recebe ----

    @Bean
    @Primary
    public ConsultaCep consultaCep(@Qualifier("provedorCep") ConsultaCep provedor) {
        return new ConsultaCepProxy(provedor);
    }

    private static FonteJson fonte(String modo, FonteJson simulada, String urlModelo) {
        return "simulado".equalsIgnoreCase(modo) ? simulada : new HttpFonteJson(urlModelo);
    }
}
