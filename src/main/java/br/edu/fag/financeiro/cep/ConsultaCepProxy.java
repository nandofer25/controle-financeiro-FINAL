package br.edu.fag.financeiro.cep;

import br.edu.fag.financeiro.logger.AppLogger;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

/**
 * PROXY de ConsultaCep (mesma interface do provedor real):
 *  1) bloqueia CEP fora do formato (8 digitos) ANTES de chamar o provedor;
 *  2) guarda em cache (Map em memoria) os CEPs ja consultados;
 *  3) registra no AppLogger se a resposta veio do CACHE ou do PROVEDOR.
 *
 * Cache feito na mao, sem @Cacheable. So respostas de sucesso entram no cache
 * (CEP inexistente ou erro do provedor nao ficam "presos" la).
 */
public class ConsultaCepProxy implements ConsultaCep {

    private static final Pattern OITO_DIGITOS = Pattern.compile("\\d{8}");

    private final AppLogger log = AppLogger.getInstance();
    private final Map<String, Endereco> cache = new ConcurrentHashMap<>();
    private final ConsultaCep provedor;

    public ConsultaCepProxy(ConsultaCep provedor) {
        this.provedor = provedor;
    }

    @Override
    public Endereco buscar(String cep) {
        // 1) validacao: nao chega no provedor se o formato estiver errado.
        // (o valor recebido nao vai para o log: poderia conter quebra de linha e forjar linhas de log)
        if (cep == null || !OITO_DIGITOS.matcher(cep).matches()) {
            log.erro(getClass(), "[Proxy] CEP BLOQUEADO: formato invalido (esperado: 8 digitos). Provedor nao foi chamado");
            throw new CepInvalidoException("CEP invalido: informe 8 digitos, sem hifen (ex.: 01001000)");
        }

        // 2) cache
        Endereco guardado = cache.get(cep);
        if (guardado != null) {
            log.info(getClass(), "[Proxy] CEP " + cep + " -> resposta veio do CACHE");
            return guardado;
        }

        // 3) provedor
        log.info(getClass(), "[Proxy] CEP " + cep + " nao esta no cache -> consultando o PROVEDOR");
        Endereco encontrado = provedor.buscar(cep);
        cache.put(cep, encontrado);
        log.info(getClass(), "[Proxy] CEP " + cep + " -> resposta veio do PROVEDOR e foi guardada no cache (" + cache.size() + " no cache)");
        return encontrado;
    }
}
