package br.edu.fag.financeiro.evento;

import br.edu.fag.financeiro.logger.AppLogger;
import br.edu.fag.financeiro.model.TipoTransacao;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Observador do alerta de gasto individual (regra que antes ficava dentro do TransacaoService):
 * despesa acima de financeiro.limite-alerta-despesa gera "ALERTA DE GASTO" no log.
 */
@Component
public class AlertaLimiteDespesaObserver implements ObservadorTransacao {

    private final AppLogger log = AppLogger.getInstance();
    private final BigDecimal limiteAlertaDespesa;

    public AlertaLimiteDespesaObserver(@Value("${financeiro.limite-alerta-despesa:500.00}") BigDecimal limiteAlertaDespesa) {
        this.limiteAlertaDespesa = limiteAlertaDespesa;
    }

    @Override
    public void aoOcorrer(EventoTransacao evento) {
        if (evento.tipo() == TipoEvento.EXCLUIDA) {
            return;
        }
        DadosTransacao t = evento.atual();
        if (t.tipo() == TipoTransacao.DESPESA && t.valor().compareTo(limiteAlertaDespesa) > 0) {
            log.info(getClass(), "ALERTA DE GASTO: despesa id=" + t.id() + " (" + t.valor()
                    + ") acima do limite de " + limiteAlertaDespesa);
        }
    }
}
