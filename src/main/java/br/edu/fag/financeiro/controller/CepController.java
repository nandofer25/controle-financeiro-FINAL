package br.edu.fag.financeiro.controller;

import br.edu.fag.financeiro.cep.ConsultaCep;
import br.edu.fag.financeiro.cep.Endereco;
import br.edu.fag.financeiro.logger.AppLogger;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller do CEP: conhece SOMENTE a interface ConsultaCep.
 * Nao sabe qual provedor esta ativo nem que existe um Proxy no meio.
 */
@RestController
@RequestMapping("/cep")
public class CepController {

    private final AppLogger log = AppLogger.getInstance();

    private final ConsultaCep consultaCep;

    public CepController(ConsultaCep consultaCep) {
        this.consultaCep = consultaCep;
    }

    // GET /cep/{cep} -> 200 OK, 400 (formato), 404 (nao existe) ou 502 (provedor com falha)
    @GetMapping("/{cep}")
    public ResponseEntity<Endereco> buscar(@PathVariable String cep) {
        log.debug(getClass(), "GET /cep/{cep} recebido");
        Endereco endereco = consultaCep.buscar(cep);
        log.info(getClass(), "GET /cep/{cep} -> 200 OK");
        return ResponseEntity.ok(endereco);
    }
}
