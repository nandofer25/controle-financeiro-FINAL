package br.edu.fag.financeiro.evento;

import br.edu.fag.financeiro.model.TipoTransacao;
import br.edu.fag.financeiro.orcamento.OrcamentoService;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** O observador decide O QUE reavaliar a partir do evento (despesa -> categoria; receita -> mes inteiro). */
class OrcamentoObserverTest {

    /** Service "espiao": registra as chamadas, sem banco. */
    static class ServiceEspiao extends OrcamentoService {
        final List<String> chamadas = new ArrayList<>();

        ServiceEspiao() {
            super(null, null, null, null);
        }

        @Override
        public void reavaliarCategoria(String categoria, int ano, int mes) {
            chamadas.add("categoria:" + categoria + ":" + mes + "/" + ano);
        }

        @Override
        public void reavaliarMes(int ano, int mes) {
            chamadas.add("mes:" + mes + "/" + ano);
        }
    }

    private static DadosTransacao dados(TipoTransacao tipo, String categoria, LocalDate data) {
        return new DadosTransacao(1L, tipo, new BigDecimal("10"), categoria, data);
    }

    @Test
    void despesaReavaliaSoACategoria() {
        ServiceEspiao service = new ServiceEspiao();
        new OrcamentoObserver(service).aoOcorrer(new EventoTransacao(TipoEvento.CRIADA,
                dados(TipoTransacao.DESPESA, "Lazer", LocalDate.of(2026, 11, 5)), null));
        assertEquals(List.of("categoria:Lazer:11/2026"), service.chamadas);
    }

    @Test
    void receitaReavaliaOMesTodo() {
        ServiceEspiao service = new ServiceEspiao();
        new OrcamentoObserver(service).aoOcorrer(new EventoTransacao(TipoEvento.CRIADA,
                dados(TipoTransacao.RECEITA, "Trabalho", LocalDate.of(2026, 11, 5)), null));
        assertEquals(List.of("mes:11/2026"), service.chamadas);
    }

    @Test
    void atualizacaoReavaliaTambemASituacaoAnterior() {
        ServiceEspiao service = new ServiceEspiao();
        new OrcamentoObserver(service).aoOcorrer(new EventoTransacao(TipoEvento.ATUALIZADA,
                dados(TipoTransacao.DESPESA, "Lazer", LocalDate.of(2026, 12, 1)),
                dados(TipoTransacao.DESPESA, "Mercado", LocalDate.of(2026, 11, 30))));
        assertEquals(List.of("categoria:Lazer:12/2026", "categoria:Mercado:11/2026"), service.chamadas);
    }

    @Test
    void despesaSemCategoriaNaoReavalia() {
        ServiceEspiao service = new ServiceEspiao();
        new OrcamentoObserver(service).aoOcorrer(new EventoTransacao(TipoEvento.CRIADA,
                dados(TipoTransacao.DESPESA, null, LocalDate.of(2026, 11, 5)), null));
        assertEquals(List.of(), service.chamadas);
    }
}
