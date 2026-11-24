package br.edu.fag.financeiro.orcamento.estrategia;

import br.edu.fag.financeiro.orcamento.TipoLimite;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** O limite acompanha a renda do mes (ex.: "Lazer: 10% das receitas"). */
@Component
public class LimitePercentualReceita implements EstrategiaLimite {

    private static final BigDecimal CEM = BigDecimal.valueOf(100);

    @Override
    public TipoLimite tipo() {
        return TipoLimite.PERCENTUAL_DA_RECEITA;
    }

    @Override
    public BigDecimal calcular(BigDecimal valorConfigurado, BigDecimal receitasDoMes) {
        return receitasDoMes.multiply(valorConfigurado).divide(CEM, 2, RoundingMode.HALF_UP);
    }
}
