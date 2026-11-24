package br.edu.fag.financeiro.cep.provedor;

import br.edu.fag.financeiro.cep.CepNaoEncontradoException;
import br.edu.fag.financeiro.cep.ProvedorCepException;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

/** Faz o GET de verdade no provedor. O modelo de URL tem um "%s" no lugar do CEP. */
public class HttpFonteJson implements FonteJson {

    private final HttpClient http = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    private final String modeloUrl;

    public HttpFonteJson(String modeloUrl) {
        this.modeloUrl = modeloUrl;
    }

    @Override
    public String consultar(String cep) {
        // encode: mesmo que alguem chame sem o Proxy, o CEP nao consegue alterar o caminho da URL
        URI uri = URI.create(modeloUrl.formatted(URLEncoder.encode(cep, StandardCharsets.UTF_8)));
        HttpRequest requisicao = HttpRequest.newBuilder(uri)
                .timeout(Duration.ofSeconds(5))
                .header("Accept", "application/json")
                .GET()
                .build();
        try {
            HttpResponse<String> resposta = http.send(requisicao, HttpResponse.BodyHandlers.ofString());
            int status = resposta.statusCode();
            if (status == 404 || status == 400) {
                throw new CepNaoEncontradoException("CEP nao encontrado no provedor");
            }
            if (status != 200) {
                throw new ProvedorCepException("Provedor respondeu HTTP " + status);
            }
            return resposta.body();
        } catch (IOException e) {
            throw new ProvedorCepException("Nao foi possivel falar com o provedor de CEP", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ProvedorCepException("Consulta de CEP interrompida", e);
        }
    }
}
