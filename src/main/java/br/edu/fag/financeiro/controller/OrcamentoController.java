package br.edu.fag.financeiro.controller;

import br.edu.fag.financeiro.logger.AppLogger;
import br.edu.fag.financeiro.orcamento.AlertaOrcamento;
import br.edu.fag.financeiro.orcamento.LimiteRequest;
import br.edu.fag.financeiro.orcamento.OrcamentoDetalhe;
import br.edu.fag.financeiro.orcamento.OrcamentoRequest;
import br.edu.fag.financeiro.orcamento.OrcamentoService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

/**
 * Controller do orcamento: so recebe, chama o Service e devolve o status HTTP.
 * Erros (404, 409, 400) sao traduzidos pelo GlobalExceptionHandler.
 */
@RestController
public class OrcamentoController {

    private final AppLogger log = AppLogger.getInstance();

    private final OrcamentoService service;

    public OrcamentoController(OrcamentoService service) {
        this.service = service;
    }

    // POST /orcamentos -> 201 Created (400 dados invalidos, 409 duplicado)
    @PostMapping("/orcamentos")
    public ResponseEntity<OrcamentoDetalhe> criar(@Valid @RequestBody OrcamentoRequest req) {
        log.debug(getClass(), "POST /orcamentos recebido");
        OrcamentoDetalhe criado = service.criar(req);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(criado.id()).toUri();
        log.info(getClass(), "POST /orcamentos -> 201 Created (id=" + criado.id() + ")");
        return ResponseEntity.created(location).body(criado);
    }

    // GET /orcamentos?ano=2026&mes=11 -> 200 OK (filtros opcionais)
    @GetMapping("/orcamentos")
    public ResponseEntity<List<OrcamentoDetalhe>> listar(@RequestParam(required = false) Integer ano,
                                                          @RequestParam(required = false) Integer mes) {
        log.debug(getClass(), "GET /orcamentos recebido");
        List<OrcamentoDetalhe> lista = service.listar(ano, mes);
        log.info(getClass(), "GET /orcamentos -> 200 OK (" + lista.size() + " itens)");
        return ResponseEntity.ok(lista);
    }

    // GET /orcamentos/{id} -> 200 OK ou 404
    @GetMapping("/orcamentos/{id}")
    public ResponseEntity<OrcamentoDetalhe> buscar(@PathVariable Long id) {
        log.debug(getClass(), "GET /orcamentos/" + id + " recebido");
        OrcamentoDetalhe detalhe = service.buscar(id);
        log.info(getClass(), "GET /orcamentos/" + id + " -> 200 OK");
        return ResponseEntity.ok(detalhe);
    }

    // PUT /orcamentos/{id}/limite -> 200 OK, 404 ou 409 (encerrado)
    @PutMapping("/orcamentos/{id}/limite")
    public ResponseEntity<OrcamentoDetalhe> alterarLimite(@PathVariable Long id, @Valid @RequestBody LimiteRequest req) {
        log.debug(getClass(), "PUT /orcamentos/" + id + "/limite recebido");
        OrcamentoDetalhe detalhe = service.alterarLimite(id, req);
        log.info(getClass(), "PUT /orcamentos/" + id + "/limite -> 200 OK");
        return ResponseEntity.ok(detalhe);
    }

    // POST /orcamentos/{id}/encerrar -> 200 OK, 404 ou 409 (ja encerrado)
    @PostMapping("/orcamentos/{id}/encerrar")
    public ResponseEntity<OrcamentoDetalhe> encerrar(@PathVariable Long id) {
        log.debug(getClass(), "POST /orcamentos/" + id + "/encerrar recebido");
        OrcamentoDetalhe detalhe = service.encerrar(id);
        log.info(getClass(), "POST /orcamentos/" + id + "/encerrar -> 200 OK");
        return ResponseEntity.ok(detalhe);
    }

    // POST /orcamentos/{id}/reabrir -> 200 OK, 404 ou 409 (nao esta encerrado)
    @PostMapping("/orcamentos/{id}/reabrir")
    public ResponseEntity<OrcamentoDetalhe> reabrir(@PathVariable Long id) {
        log.debug(getClass(), "POST /orcamentos/" + id + "/reabrir recebido");
        OrcamentoDetalhe detalhe = service.reabrir(id);
        log.info(getClass(), "POST /orcamentos/" + id + "/reabrir -> 200 OK");
        return ResponseEntity.ok(detalhe);
    }

    // DELETE /orcamentos/{id} -> 204 No Content ou 404
    @DeleteMapping("/orcamentos/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        log.debug(getClass(), "DELETE /orcamentos/" + id + " recebido");
        service.excluir(id);
        log.info(getClass(), "DELETE /orcamentos/" + id + " -> 204 No Content");
        return ResponseEntity.noContent().build();
    }

    // GET /alertas -> 200 OK (alertas gerados quando orcamentos entram em ATENCAO/ESTOURADO, mais recentes primeiro)
    @GetMapping("/alertas")
    public ResponseEntity<List<AlertaOrcamento>> alertas() {
        log.debug(getClass(), "GET /alertas recebido");
        List<AlertaOrcamento> lista = service.listarAlertas();
        log.info(getClass(), "GET /alertas -> 200 OK (" + lista.size() + " itens)");
        return ResponseEntity.ok(lista);
    }
}
