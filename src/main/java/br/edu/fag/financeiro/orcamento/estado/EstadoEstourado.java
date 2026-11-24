package br.edu.fag.financeiro.orcamento.estado;

import br.edu.fag.financeiro.orcamento.Orcamento;
import br.edu.fag.financeiro.orcamento.StatusOrcamento;

import java.math.BigDecimal;
import java.util.Optional;

/** Consumo acima do limite. Gera alerta de estouro. */
class EstadoEstourado extends EstadoAberto {

    @Override
    public StatusOrcamento status() {
        return StatusOrcamento.ESTOURADO;
    }

    @Override
    public Optional<String> mensagemDeEntrada(Orcamento o, BigDecimal consumido, BigDecimal limite) {
        return Optional.of("ESTOURO: categoria '" + o.getCategoria() + "' gastou " + consumido
                + ", acima do limite de " + limite);
    }
}
