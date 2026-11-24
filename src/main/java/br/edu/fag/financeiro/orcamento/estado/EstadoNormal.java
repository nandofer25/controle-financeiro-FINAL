package br.edu.fag.financeiro.orcamento.estado;

import br.edu.fag.financeiro.orcamento.Orcamento;
import br.edu.fag.financeiro.orcamento.StatusOrcamento;

import java.math.BigDecimal;
import java.util.Optional;

/** Consumo abaixo de 80% do limite. Nao gera alerta. */
class EstadoNormal extends EstadoAberto {

    @Override
    public StatusOrcamento status() {
        return StatusOrcamento.NORMAL;
    }

    @Override
    public Optional<String> mensagemDeEntrada(Orcamento o, BigDecimal consumido, BigDecimal limite) {
        return Optional.empty();
    }
}
