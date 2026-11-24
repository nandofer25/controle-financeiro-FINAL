package br.edu.fag.financeiro.orcamento;

import br.edu.fag.financeiro.orcamento.estrategia.EstrategiaLimite;
import br.edu.fag.financeiro.orcamento.estrategia.LimiteFixo;
import br.edu.fag.financeiro.orcamento.estrategia.LimitePercentualReceita;
import br.edu.fag.financeiro.orcamento.estrategia.SeletorEstrategiaLimite;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** STRATEGY: cada estrategia calcula o limite de um jeito; o seletor escolhe sem if/switch. */
class EstrategiaLimiteTest {

    @Test
    void limiteFixoIgnoraAsReceitas() {
        BigDecimal limite = new LimiteFixo().calcular(new BigDecimal("800"), new BigDecimal("9999"));
        assertEquals(new BigDecimal("800.00"), limite);
    }

    @Test
    void limitePercentualAcompanhaAsReceitas() {
        BigDecimal limite = new LimitePercentualReceita().calcular(new BigDecimal("30"), new BigDecimal("5000.00"));
        assertEquals(new BigDecimal("1500.00"), limite);
    }

    @Test
    void limitePercentualSemReceitaDaZero() {
        BigDecimal limite = new LimitePercentualReceita().calcular(new BigDecimal("30"), BigDecimal.ZERO);
        assertEquals(0, limite.signum());
    }

    @Test
    void seletorEscolhePelaTipo() {
        SeletorEstrategiaLimite seletor = new SeletorEstrategiaLimite(List.of(new LimiteFixo(), new LimitePercentualReceita()));
        assertTrue(seletor.para(TipoLimite.FIXO) instanceof LimiteFixo);
        assertTrue(seletor.para(TipoLimite.PERCENTUAL_DA_RECEITA) instanceof LimitePercentualReceita);
    }

    @Test
    void novaEstrategiaEntraSemMudarOSeletor() {
        // OCP: uma estrategia nova (aqui anonima) e aceita so por implementar a interface
        EstrategiaLimite dobro = new EstrategiaLimite() {
            public TipoLimite tipo() { return TipoLimite.FIXO; }
            public BigDecimal calcular(BigDecimal v, BigDecimal r) { return v.multiply(BigDecimal.valueOf(2)); }
        };
        SeletorEstrategiaLimite seletor = new SeletorEstrategiaLimite(List.of(dobro));
        assertEquals(new BigDecimal("200"), seletor.para(TipoLimite.FIXO).calcular(new BigDecimal("100"), BigDecimal.ZERO));
    }

    @Test
    void tipoSemEstrategiaFalhaComMensagemClara() {
        SeletorEstrategiaLimite seletor = new SeletorEstrategiaLimite(List.of(new LimiteFixo()));
        assertThrows(IllegalStateException.class, () -> seletor.para(TipoLimite.PERCENTUAL_DA_RECEITA));
    }
}
