package br.edu.fag.financeiro.orcamento.estado;

import br.edu.fag.financeiro.orcamento.Orcamento;
import br.edu.fag.financeiro.orcamento.StatusOrcamento;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * STATE: o ciclo de vida de um Orcamento (NORMAL -> ATENCAO -> ESTOURADO -> ENCERRADO).
 * Cada estado decide o que pode acontecer e para qual estado o orcamento vai em seguida,
 * em vez de o Service ter um monte de if/switch sobre o status.
 * O contexto (Orcamento) delega as operacoes ao estado atual.
 */
public interface EstadoOrcamento {

    StatusOrcamento status();

    /** Reavalia o orcamento diante do consumo atual. Orcamento encerrado ignora (esta congelado). */
    void atualizarConsumo(Orcamento orcamento, BigDecimal consumido, BigDecimal limite);

    /** Fecha o mes. So e permitido em orcamento aberto. */
    void encerrar(Orcamento orcamento);

    /** Reabre um orcamento encerrado. So e permitido quando esta encerrado. */
    void reabrir(Orcamento orcamento);

    /** Chamado antes de trocar o limite: o estado pode vetar (orcamento encerrado esta congelado). */
    void aoAlterarLimite(Orcamento orcamento);

    /** Texto do alerta gerado quando o orcamento ENTRA neste estado (vazio = nao gera alerta). */
    Optional<String> mensagemDeEntrada(Orcamento orcamento, BigDecimal consumido, BigDecimal limite);
}
