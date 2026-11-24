package br.edu.fag.financeiro.orcamento.estado;

import br.edu.fag.financeiro.orcamento.Orcamento;
import br.edu.fag.financeiro.orcamento.OperacaoInvalidaException;
import br.edu.fag.financeiro.orcamento.StatusOrcamento;

import java.math.BigDecimal;

/**
 * Comportamento comum dos estados "abertos" (NORMAL, ATENCAO, ESTOURADO):
 * aceitam novo consumo e podem ser encerrados, mas nao reabertos.
 */
abstract class EstadoAberto implements EstadoOrcamento {

    private static final BigDecimal LIMIAR_ATENCAO = new BigDecimal("0.80");

    @Override
    public void atualizarConsumo(Orcamento orcamento, BigDecimal consumido, BigDecimal limite) {
        orcamento.mudarPara(classificar(consumido, limite));
    }

    @Override
    public void encerrar(Orcamento orcamento) {
        orcamento.mudarPara(StatusOrcamento.ENCERRADO);
    }

    @Override
    public void reabrir(Orcamento orcamento) {
        throw new OperacaoInvalidaException("Orcamento id=" + orcamento.getId()
                + " ja esta aberto (status " + status() + "); so e possivel reabrir um orcamento ENCERRADO");
    }

    @Override
    public void aoAlterarLimite(Orcamento orcamento) {
        // orcamento aberto aceita novo limite
    }

    /** Regra de transicao: > 100% do limite = ESTOURADO; >= 80% = ATENCAO; senao NORMAL. */
    static StatusOrcamento classificar(BigDecimal consumido, BigDecimal limite) {
        if (consumido.signum() == 0) {
            return StatusOrcamento.NORMAL;
        }
        if (consumido.compareTo(limite) > 0) {
            return StatusOrcamento.ESTOURADO;
        }
        if (consumido.compareTo(limite.multiply(LIMIAR_ATENCAO)) >= 0) {
            return StatusOrcamento.ATENCAO;
        }
        return StatusOrcamento.NORMAL;
    }
}
