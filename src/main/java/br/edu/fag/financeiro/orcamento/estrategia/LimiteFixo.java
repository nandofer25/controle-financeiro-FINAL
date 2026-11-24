package br.edu.fag.financeiro.orcamento.estrategia;

import br.edu.fag.financeiro.orcamento.TipoLimite;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** O limite e exatamente o valor informado (ex.: "Alimentacao: R$ 800,00"). */
@Component
public class LimiteFixo implements EstrategiaLimite {

    @Override
    public TipoLimite tipo() {
        return TipoLimite.FIXO;
    }

    @Override
    public BigDecimal calcular(BigDecimal valorConfigurado, BigDecimal receitasDoMes) {
        return valorConfigurado.setScale(2, RoundingMode.HALF_UP);
    }
}
