package br.edu.fag.financeiro.controller;

import br.edu.fag.financeiro.cep.CepInvalidoException;
import br.edu.fag.financeiro.cep.CepNaoEncontradoException;
import br.edu.fag.financeiro.cep.ProvedorCepException;
import br.edu.fag.financeiro.logger.AppLogger;
import br.edu.fag.financeiro.orcamento.DadosInvalidosException;
import br.edu.fag.financeiro.orcamento.OperacaoInvalidaException;
import br.edu.fag.financeiro.orcamento.OrcamentoNaoEncontradoException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Captura excecoes e registra ERRO no log. Nao registra o corpo da requisicao
 * (evita vazar dados sensiveis); loga apenas os campos invalidos / tipo da excecao.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private final AppLogger log = AppLogger.getInstance();

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> validacao(MethodArgumentNotValidException ex) {
        Map<String, String> campos = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(e -> campos.put(e.getField(), e.getDefaultMessage()));

        log.erro(getClass(), "Requisicao invalida (400). Campos com erro: " + campos.keySet());
        return ResponseEntity.badRequest().body(Map.of("erro", "Dados invalidos", "campos", campos));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, String>> jsonInvalido(HttpMessageNotReadableException ex) {
        log.erro(getClass(), "JSON ilegivel ou valor invalido na requisicao (400)", ex);
        return ResponseEntity.badRequest().body(Map.of("erro", "JSON invalido. Confira os campos e o tipo (RECEITA ou DESPESA)."));
    }

    @ExceptionHandler(CepInvalidoException.class)
    public ResponseEntity<Map<String, String>> cepInvalido(CepInvalidoException ex) {
        // o Proxy ja registrou o bloqueio no log; aqui so devolvemos o 400
        return ResponseEntity.badRequest().body(Map.of("erro", ex.getMessage()));
    }

    @ExceptionHandler(CepNaoEncontradoException.class)
    public ResponseEntity<Map<String, String>> cepNaoEncontrado(CepNaoEncontradoException ex) {
        log.erro(getClass(), "CEP nao encontrado (404)");
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("erro", ex.getMessage()));
    }

    @ExceptionHandler(ProvedorCepException.class)
    public ResponseEntity<Map<String, String>> provedorComFalha(ProvedorCepException ex) {
        log.erro(getClass(), "Falha no provedor de CEP (502)", ex);
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                .body(Map.of("erro", "Provedor de CEP indisponivel. Tente novamente mais tarde."));
    }

    // ---- Orcamento (Strategy + State + Observer) ----

    @ExceptionHandler(OrcamentoNaoEncontradoException.class)
    public ResponseEntity<Map<String, String>> orcamentoNaoEncontrado(OrcamentoNaoEncontradoException ex) {
        // o Service ja registrou o ERRO no log
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("erro", ex.getMessage()));
    }

    @ExceptionHandler(OperacaoInvalidaException.class)
    public ResponseEntity<Map<String, String>> operacaoInvalida(OperacaoInvalidaException ex) {
        log.erro(getClass(), "Operacao incompativel com o estado do orcamento (409): " + ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("erro", ex.getMessage()));
    }

    @ExceptionHandler(DadosInvalidosException.class)
    public ResponseEntity<Map<String, String>> dadosInvalidos(DadosInvalidosException ex) {
        log.erro(getClass(), "Dados invalidos para o orcamento (400): " + ex.getMessage());
        return ResponseEntity.badRequest().body(Map.of("erro", ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> generica(Exception ex) {
        log.erro(getClass(), "Erro inesperado (500)", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("erro", "Erro interno do servidor"));
    }
}
