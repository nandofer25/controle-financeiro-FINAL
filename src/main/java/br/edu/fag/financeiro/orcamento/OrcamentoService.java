package br.edu.fag.financeiro.orcamento;

import br.edu.fag.financeiro.logger.AppLogger;
import br.edu.fag.financeiro.model.TipoTransacao;
import br.edu.fag.financeiro.orcamento.estrategia.SeletorEstrategiaLimite;
import br.edu.fag.financeiro.repository.TransacaoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

/**
 * Regras de negocio do orcamento por categoria.
 *  - STRATEGY: o limite efetivo vem de uma EstrategiaLimite (fixo ou % da receita).
 *  - STATE: as transicoes e operacoes permitidas ficam nas classes de orcamento/estado.
 * Este Service so orquestra: nao tem if/switch sobre tipo de limite nem sobre status.
 */
@Service
public class OrcamentoService {

    private static final BigDecimal CEM = BigDecimal.valueOf(100);

    private final AppLogger log = AppLogger.getInstance();

    private final OrcamentoRepository orcamentos;
    private final AlertaOrcamentoRepository alertas;
    private final TransacaoRepository transacoes;
    private final SeletorEstrategiaLimite estrategias;

    public OrcamentoService(OrcamentoRepository orcamentos,
                            AlertaOrcamentoRepository alertas,
                            TransacaoRepository transacoes,
                            SeletorEstrategiaLimite estrategias) {
        this.orcamentos = orcamentos;
        this.alertas = alertas;
        this.transacoes = transacoes;
        this.estrategias = estrategias;
    }

    // ------------------------------------------------------------------ casos de uso (API)

    @Transactional
    public OrcamentoDetalhe criar(OrcamentoRequest req) {
        String categoria = req.categoria().trim();
        log.debug(getClass(), "Criando orcamento: categoria=" + categoria + ", " + req.mes() + "/" + req.ano()
                + ", tipoLimite=" + req.tipoLimite() + ", valorLimite=" + req.valorLimite());

        validarLimite(req.tipoLimite(), req.valorLimite());
        if (orcamentos.findByCategoriaIgnoreCaseAndAnoAndMes(categoria, req.ano(), req.mes()).isPresent()) {
            log.erro(getClass(), "Orcamento duplicado para a categoria e mes informados");
            throw new OperacaoInvalidaException("Ja existe orcamento da categoria '" + categoria
                    + "' em " + req.mes() + "/" + req.ano());
        }

        Orcamento orcamento = orcamentos.save(
                new Orcamento(categoria, req.ano(), req.mes(), req.tipoLimite(), req.valorLimite()));
        avaliar(orcamento); // ja pode haver gastos no mes: o orcamento nasce no estado correto
        log.info(getClass(), "Orcamento criado com sucesso: id=" + orcamento.getId());
        return detalhar(orcamento);
    }

    @Transactional(readOnly = true)
    public List<OrcamentoDetalhe> listar(Integer ano, Integer mes) {
        log.debug(getClass(), "Listando orcamentos (ano=" + ano + ", mes=" + mes + ")");
        List<OrcamentoDetalhe> lista = orcamentos.findAll().stream()
                .filter(o -> ano == null || o.getAno() == ano)
                .filter(o -> mes == null || o.getMes() == mes)
                .map(this::detalhar)
                .toList();
        log.info(getClass(), "Listagem de orcamentos concluida: " + lista.size() + " item(ns)");
        return lista;
    }

    @Transactional(readOnly = true)
    public OrcamentoDetalhe buscar(Long id) {
        log.debug(getClass(), "Buscando orcamento id=" + id);
        return detalhar(obter(id));
    }

    @Transactional
    public OrcamentoDetalhe alterarLimite(Long id, LimiteRequest req) {
        log.debug(getClass(), "Alterando limite do orcamento id=" + id + " para " + req.tipoLimite() + " " + req.valorLimite());
        validarLimite(req.tipoLimite(), req.valorLimite());
        Orcamento orcamento = obter(id);
        orcamento.redefinirLimite(req.tipoLimite(), req.valorLimite()); // State: encerrado nao aceita
        avaliar(orcamento);
        log.info(getClass(), "Limite do orcamento alterado com sucesso: id=" + id);
        return detalhar(orcamento);
    }

    @Transactional
    public OrcamentoDetalhe encerrar(Long id) {
        log.debug(getClass(), "Encerrando orcamento id=" + id);
        Orcamento orcamento = obter(id);
        orcamento.encerrar(); // State: so permitido se estiver aberto
        orcamentos.save(orcamento);
        log.info(getClass(), "Orcamento encerrado: id=" + id);
        return detalhar(orcamento);
    }

    @Transactional
    public OrcamentoDetalhe reabrir(Long id) {
        log.debug(getClass(), "Reabrindo orcamento id=" + id);
        Orcamento orcamento = obter(id);
        orcamento.reabrir(); // State: so permitido se estiver encerrado
        avaliar(orcamento);  // recalcula com os lancamentos feitos enquanto estava encerrado
        log.info(getClass(), "Orcamento reaberto: id=" + id + ", status=" + orcamento.getStatus());
        return detalhar(orcamento);
    }

    @Transactional
    public void excluir(Long id) {
        log.debug(getClass(), "Excluindo orcamento id=" + id);
        Orcamento orcamento = obter(id);
        orcamentos.delete(orcamento);
        log.info(getClass(), "Orcamento excluido com sucesso: id=" + id);
    }

    @Transactional(readOnly = true)
    public List<AlertaOrcamento> listarAlertas() {
        log.debug(getClass(), "Listando alertas de orcamento");
        List<AlertaOrcamento> lista = alertas.findAllByOrderByIdDesc();
        log.info(getClass(), "Listagem de alertas concluida: " + lista.size() + " alerta(s)");
        return lista;
    }

    // ------------------------------------------------------------------ chamados pelo Observer

    /** Uma despesa da categoria mudou: reavalia o orcamento dela naquele mes (se existir). */
    @Transactional
    public void reavaliarCategoria(String categoria, int ano, int mes) {
        orcamentos.findByCategoriaIgnoreCaseAndAnoAndMes(categoria.trim(), ano, mes).ifPresent(this::avaliar);
    }

    /** Uma receita mudou: afeta limites percentuais, entao reavalia todos os orcamentos do mes. */
    @Transactional
    public void reavaliarMes(int ano, int mes) {
        orcamentos.findByAnoAndMes(ano, mes).forEach(this::avaliar);
    }

    // ------------------------------------------------------------------ internos

    /** Recalcula consumo e limite, deixa o State decidir a transicao e registra alerta se entrou em ATENCAO/ESTOURADO. */
    private void avaliar(Orcamento orcamento) {
        Consumo c = calcularConsumo(orcamento);
        StatusOrcamento antes = orcamento.getStatus();

        orcamento.atualizarConsumo(c.consumido(), c.limite());

        StatusOrcamento depois = orcamento.getStatus();
        if (antes != depois) {
            log.info(getClass(), "Orcamento id=" + orcamento.getId() + " mudou de estado: " + antes + " -> " + depois);
            orcamento.mensagemDeEntrada(c.consumido(), c.limite()).ifPresent(msg -> {
                alertas.save(new AlertaOrcamento(orcamento.getId(), orcamento.getCategoria(), depois, msg));
                log.info(getClass(), "ALERTA DE ORCAMENTO: " + msg);
            });
        }
        orcamentos.save(orcamento);
    }

    private Consumo calcularConsumo(Orcamento o) {
        YearMonth periodo = YearMonth.of(o.getAno(), o.getMes());
        LocalDate inicio = periodo.atDay(1);
        LocalDate fim = periodo.atEndOfMonth();

        BigDecimal receitas = transacoes.somarPorTipoNoPeriodo(TipoTransacao.RECEITA, inicio, fim);
        BigDecimal consumido = transacoes.somarPorTipoECategoriaNoPeriodo(TipoTransacao.DESPESA, o.getCategoria(), inicio, fim);
        BigDecimal limite = estrategias.para(o.getTipoLimite()).calcular(o.getValorLimite(), receitas);
        return new Consumo(consumido, limite);
    }

    private OrcamentoDetalhe detalhar(Orcamento o) {
        Consumo c = calcularConsumo(o);
        BigDecimal percentual = c.limite().signum() == 0
                ? null
                : c.consumido().multiply(CEM).divide(c.limite(), 2, RoundingMode.HALF_UP);
        return new OrcamentoDetalhe(o.getId(), o.getCategoria(), o.getAno(), o.getMes(), o.getTipoLimite(),
                o.getValorLimite(), c.limite(), c.consumido(), c.limite().subtract(c.consumido()),
                percentual, o.getStatus());
    }

    private Orcamento obter(Long id) {
        return orcamentos.findById(id).orElseThrow(() -> {
            log.erro(getClass(), "Orcamento nao encontrado: id=" + id);
            return new OrcamentoNaoEncontradoException(id);
        });
    }

    private void validarLimite(TipoLimite tipo, BigDecimal valor) {
        if (tipo == TipoLimite.PERCENTUAL_DA_RECEITA && valor.compareTo(CEM) > 0) {
            throw new DadosInvalidosException("Para PERCENTUAL_DA_RECEITA, valorLimite deve ser de 0 a 100");
        }
    }

    private record Consumo(BigDecimal consumido, BigDecimal limite) {
    }
}
