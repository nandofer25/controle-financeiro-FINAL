package br.edu.fag.financeiro.orcamento.estrategia;

import br.edu.fag.financeiro.orcamento.TipoLimite;

import java.math.BigDecimal;

/**
 * STRATEGY: define COMO o limite de um orcamento e calculado.
 * O OrcamentoService so conhece esta interface; novas formas de calcular
 * o limite = nova classe que a implementa, sem alterar o Service (OCP).
 */
public interface EstrategiaLimite {

    /** Tipo de limite que esta estrategia atende (usado pelo seletor). */
    TipoLimite tipo();

    /**
     * @param valorConfigurado valor guardado no orcamento (reais ou percentual, conforme a estrategia)
     * @param receitasDoMes    total de receitas do mes do orcamento
     * @return limite efetivo em reais
     */
    BigDecimal calcular(BigDecimal valorConfigurado, BigDecimal receitasDoMes);
}
