package br.edu.fag.financeiro.orcamento;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/** STATE: transicoes automaticas por consumo e operacoes bloqueadas conforme o estado. */
class EstadoOrcamentoTest {

    private static final BigDecimal LIMITE = new BigDecimal("1000.00");

    private Orcamento novo() {
        return new Orcamento("Alimentacao", 2026, 11, TipoLimite.FIXO, LIMITE);
    }

    private static BigDecimal v(String valor) {
        return new BigDecimal(valor);
    }

    @Test
    void comecaNormal() {
        assertEquals(StatusOrcamento.NORMAL, novo().getStatus());
    }

    @Test
    void transicoesPeloConsumo() {
        Orcamento o = novo();

        o.atualizarConsumo(v("799.99"), LIMITE);
        assertEquals(StatusOrcamento.NORMAL, o.getStatus());

        o.atualizarConsumo(v("800.00"), LIMITE);
        assertEquals(StatusOrcamento.ATENCAO, o.getStatus());

        o.atualizarConsumo(v("1000.00"), LIMITE); // exatamente no limite ainda nao estourou
        assertEquals(StatusOrcamento.ATENCAO, o.getStatus());

        o.atualizarConsumo(v("1000.01"), LIMITE);
        assertEquals(StatusOrcamento.ESTOURADO, o.getStatus());
    }

    @Test
    void estouradoVoltaQuandoOConsumoCai() {
        Orcamento o = novo();
        o.atualizarConsumo(v("1500"), LIMITE);
        assertEquals(StatusOrcamento.ESTOURADO, o.getStatus());

        o.atualizarConsumo(v("900"), LIMITE);
        assertEquals(StatusOrcamento.ATENCAO, o.getStatus());

        o.atualizarConsumo(v("100"), LIMITE);
        assertEquals(StatusOrcamento.NORMAL, o.getStatus());
    }

    @Test
    void semConsumoENormalMesmoComLimiteZero() {
        Orcamento o = novo();
        o.atualizarConsumo(BigDecimal.ZERO, BigDecimal.ZERO);
        assertEquals(StatusOrcamento.NORMAL, o.getStatus());
    }

    @Test
    void consumoComLimiteZeroEstoura() {
        Orcamento o = novo();
        o.atualizarConsumo(v("10"), BigDecimal.ZERO);
        assertEquals(StatusOrcamento.ESTOURADO, o.getStatus());
    }

    @Test
    void alertaSoNosEstadosDeAtencaoEEstouro() {
        Orcamento o = novo();
        assertTrue(o.mensagemDeEntrada(v("100"), LIMITE).isEmpty());

        o.atualizarConsumo(v("850"), LIMITE);
        assertTrue(o.mensagemDeEntrada(v("850"), LIMITE).orElseThrow().contains("Atencao"));

        o.atualizarConsumo(v("1200"), LIMITE);
        assertTrue(o.mensagemDeEntrada(v("1200"), LIMITE).orElseThrow().contains("ESTOURO"));
    }

    @Test
    void podeEncerrarQualquerEstadoAberto() {
        for (String consumo : new String[]{"0", "850", "1200"}) {
            Orcamento o = novo();
            o.atualizarConsumo(v(consumo), LIMITE);
            o.encerrar();
            assertEquals(StatusOrcamento.ENCERRADO, o.getStatus());
        }
    }

    @Test
    void naoPodeEncerrarDuasVezes() {
        Orcamento o = novo();
        o.encerrar();
        assertThrows(OperacaoInvalidaException.class, o::encerrar);
    }

    @Test
    void naoPodeReabrirOrcamentoAberto() {
        Orcamento o = novo();
        assertThrows(OperacaoInvalidaException.class, o::reabrir);

        o.atualizarConsumo(v("1200"), LIMITE);
        assertThrows(OperacaoInvalidaException.class, o::reabrir);
    }

    @Test
    void encerradoFicaCongeladoAteReabrir() {
        Orcamento o = novo();
        o.atualizarConsumo(v("100"), LIMITE);
        o.encerrar();

        o.atualizarConsumo(v("5000"), LIMITE); // novos gastos nao mexem no orcamento encerrado
        assertEquals(StatusOrcamento.ENCERRADO, o.getStatus());

        o.reabrir();
        assertEquals(StatusOrcamento.NORMAL, o.getStatus());
        o.atualizarConsumo(v("5000"), LIMITE);
        assertEquals(StatusOrcamento.ESTOURADO, o.getStatus());
    }

    @Test
    void encerradoNaoAceitaNovoLimite() {
        Orcamento o = novo();
        o.encerrar();
        assertThrows(OperacaoInvalidaException.class, () -> o.redefinirLimite(TipoLimite.FIXO, v("2000")));

        o.reabrir();
        o.redefinirLimite(TipoLimite.PERCENTUAL_DA_RECEITA, v("25"));
        assertEquals(TipoLimite.PERCENTUAL_DA_RECEITA, o.getTipoLimite());
        assertEquals(v("25"), o.getValorLimite());
    }
}
