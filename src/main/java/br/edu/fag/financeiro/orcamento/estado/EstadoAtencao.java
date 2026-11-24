package br.edu.fag.financeiro.orcamento.estado;

import br.edu.fag.financeiro.orcamento.Orcamento;
import br.edu.fag.financeiro.orcamento.StatusOrcamento;

import java.math.BigDecimal;
import java.util.Optional;

/** Consumo entre 80% e 100% do limite. Gera aviso preventivo. */
class EstadoAtencao extends EstadoAberto {

    @Override
    public StatusOrcamento status() {
        return StatusOrcamento.ATENCAO;
    }

    @Override
    public Optional<String> mensagemDeEntrada(Orcamento o, BigDecimal consumido, BigDecimal limite) {
        return Optional.of("Atencao: categoria '" + o.getCategoria() + "' consumiu " + consumido
                + " de " + limite + " (80% ou mais do limite)");
    }
}
