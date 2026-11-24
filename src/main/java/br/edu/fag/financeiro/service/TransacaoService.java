package br.edu.fag.financeiro.service;

import br.edu.fag.financeiro.evento.DadosTransacao;
import br.edu.fag.financeiro.evento.EventoTransacao;
import br.edu.fag.financeiro.evento.SujeitoTransacao;
import br.edu.fag.financeiro.evento.TipoEvento;
import br.edu.fag.financeiro.logger.AppLogger;
import br.edu.fag.financeiro.model.ResumoFinanceiro;
import br.edu.fag.financeiro.model.TipoTransacao;
import br.edu.fag.financeiro.model.Transacao;
import br.edu.fag.financeiro.repository.TransacaoRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Regras de negocio das transacoes. O Controller so recebe e devolve; a logica fica aqui.
 *
 * OBSERVER: depois de gravar, o Service apenas PUBLICA um evento no SujeitoTransacao. Quem reage
 * (alerta de gasto individual, atualizacao dos orcamentos, ...) sao observadores que o Service nao conhece.
 *
 * VERSAO FINAL DO PROJETO: usa o AppLogger Singleton classico (ver README.md, secao "Justificativa").
 */
@Service
public class TransacaoService {

    // Singleton classico: nao e injetado, e obtido pelo getInstance()
    private final AppLogger log = AppLogger.getInstance();

    private final TransacaoRepository repository;
    private final SujeitoTransacao eventos;

    public TransacaoService(TransacaoRepository repository, SujeitoTransacao eventos) {
        this.repository = repository;
        this.eventos = eventos;
    }

    public Transacao criar(Transacao transacao) {
        log.debug(getClass(), "Criando transacao: tipo=" + transacao.getTipo()
                + ", categoria=" + transacao.getCategoria() + ", valor=" + transacao.getValor());

        transacao.setId(null); // garante INSERT, mesmo que o cliente envie um id
        if (transacao.getData() == null) {
            transacao.setData(LocalDate.now());
        }

        Transacao salva = repository.save(transacao);
        eventos.notificar(new EventoTransacao(TipoEvento.CRIADA, DadosTransacao.de(salva), null));

        log.info(getClass(), "Transacao criada com sucesso: id=" + salva.getId());
        return salva;
    }

    public List<Transacao> listar() {
        log.debug(getClass(), "Listando todas as transacoes");
        List<Transacao> lista = repository.findAll();
        log.info(getClass(), "Listagem concluida: " + lista.size() + " transacao(oes)");
        return lista;
    }

    public Optional<Transacao> buscarPorId(Long id) {
        log.debug(getClass(), "Buscando transacao por id=" + id);
        Optional<Transacao> encontrada = repository.findById(id);
        if (encontrada.isEmpty()) {
            log.erro(getClass(), "Transacao nao encontrada: id=" + id);
        } else {
            log.info(getClass(), "Transacao encontrada: id=" + id);
        }
        return encontrada;
    }

    public Optional<Transacao> atualizar(Long id, Transacao dados) {
        log.debug(getClass(), "Atualizando transacao id=" + id + " com tipo=" + dados.getTipo()
                + ", valor=" + dados.getValor());

        Optional<Transacao> existente = repository.findById(id);
        if (existente.isEmpty()) {
            log.erro(getClass(), "Nao foi possivel atualizar, transacao nao encontrada: id=" + id);
            return Optional.empty();
        }

        Transacao transacao = existente.get();
        DadosTransacao anterior = DadosTransacao.de(transacao); // foto de antes da alteracao

        transacao.setDescricao(dados.getDescricao());
        transacao.setValor(dados.getValor());
        transacao.setTipo(dados.getTipo());
        transacao.setCategoria(dados.getCategoria());
        transacao.setData(dados.getData() != null ? dados.getData() : transacao.getData());

        Transacao atualizada = repository.save(transacao);
        eventos.notificar(new EventoTransacao(TipoEvento.ATUALIZADA, DadosTransacao.de(atualizada), anterior));

        log.info(getClass(), "Transacao atualizada com sucesso: id=" + id);
        return Optional.of(atualizada);
    }

    public boolean excluir(Long id) {
        log.debug(getClass(), "Excluindo transacao id=" + id);
        Optional<Transacao> existente = repository.findById(id);
        if (existente.isEmpty()) {
            log.erro(getClass(), "Nao foi possivel excluir, transacao nao encontrada: id=" + id);
            return false;
        }
        DadosTransacao removida = DadosTransacao.de(existente.get());
        repository.deleteById(id);
        eventos.notificar(new EventoTransacao(TipoEvento.EXCLUIDA, removida, null));

        log.info(getClass(), "Transacao excluida com sucesso: id=" + id);
        return true;
    }

    /** Resumo de receitas x despesas. alertaGasto = true quando as despesas passam das receitas. */
    public ResumoFinanceiro resumo() {
        log.debug(getClass(), "Calculando resumo financeiro");
        BigDecimal receitas = repository.somarPorTipo(TipoTransacao.RECEITA);
        BigDecimal despesas = repository.somarPorTipo(TipoTransacao.DESPESA);
        BigDecimal saldo = receitas.subtract(despesas);
        boolean alerta = despesas.compareTo(receitas) > 0;

        if (alerta) {
            log.info(getClass(), "ALERTA DE GASTO: despesas (" + despesas + ") maiores que receitas (" + receitas + ")");
        }
        log.info(getClass(), "Resumo financeiro calculado: saldo=" + saldo);
        return new ResumoFinanceiro(receitas, despesas, saldo, alerta);
    }
}
