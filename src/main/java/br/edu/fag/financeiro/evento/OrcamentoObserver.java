package br.edu.fag.financeiro.evento;

import br.edu.fag.financeiro.logger.AppLogger;
import br.edu.fag.financeiro.model.TipoTransacao;
import br.edu.fag.financeiro.orcamento.OrcamentoService;
import org.springframework.stereotype.Component;

/**
 * Observador que mantem os orcamentos em dia: a cada mudanca de transacao, pede ao
 * OrcamentoService para reavaliar o que foi afetado (dentro do Service ficam Strategy e State).
 *
 * - DESPESA afeta o orcamento da sua categoria no mes.
 * - RECEITA afeta o consumo percentual de TODOS os orcamentos do mes (limite % da receita).
 * Em ATUALIZADA reavalia tambem a situacao anterior (categoria, tipo ou mes podem ter mudado).
 */
@Component
public class OrcamentoObserver implements ObservadorTransacao {

    private final AppLogger log = AppLogger.getInstance();
    private final OrcamentoService orcamentoService;

    public OrcamentoObserver(OrcamentoService orcamentoService) {
        this.orcamentoService = orcamentoService;
    }

    @Override
    public void aoOcorrer(EventoTransacao evento) {
        reavaliar(evento.atual());
        if (evento.anterior() != null) {
            reavaliar(evento.anterior());
        }
    }

    private void reavaliar(DadosTransacao t) {
        if (t.data() == null) {
            return;
        }
        log.debug(getClass(), "[Observer] reavaliando orcamentos afetados pela transacao id=" + t.id());
        if (t.tipo() == TipoTransacao.RECEITA) {
            orcamentoService.reavaliarMes(t.data().getYear(), t.data().getMonthValue());
        } else if (t.categoria() != null && !t.categoria().isBlank()) {
            orcamentoService.reavaliarCategoria(t.categoria(), t.data().getYear(), t.data().getMonthValue());
        }
    }
}
