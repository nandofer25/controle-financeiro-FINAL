package br.edu.fag.financeiro.evento;

import br.edu.fag.financeiro.model.TipoTransacao;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** OBSERVER: registrar, remover, notificar e isolar falhas. */
class PublicadorTransacaoTest {

    private static EventoTransacao evento() {
        DadosTransacao d = new DadosTransacao(1L, TipoTransacao.DESPESA, new BigDecimal("50"), "Lazer", LocalDate.of(2026, 11, 10));
        return new EventoTransacao(TipoEvento.CRIADA, d, null);
    }

    @Test
    void notificaTodosOsObservadoresRegistrados() {
        List<EventoTransacao> a = new ArrayList<>();
        List<EventoTransacao> b = new ArrayList<>();
        PublicadorTransacao publicador = new PublicadorTransacao(List.of());
        publicador.registrar(a::add);
        publicador.registrar(b::add);

        EventoTransacao e = evento();
        publicador.notificar(e);

        assertEquals(List.of(e), a);
        assertEquals(List.of(e), b);
    }

    @Test
    void observadoresIniciaisVindosDoSpringSaoRegistrados() {
        List<EventoTransacao> recebidos = new ArrayList<>();
        PublicadorTransacao publicador = new PublicadorTransacao(List.of(recebidos::add));
        publicador.notificar(evento());
        assertEquals(1, recebidos.size());
    }

    @Test
    void observadorRemovidoNaoRecebeMais() {
        List<EventoTransacao> recebidos = new ArrayList<>();
        ObservadorTransacao obs = recebidos::add;
        PublicadorTransacao publicador = new PublicadorTransacao(List.of());
        publicador.registrar(obs);
        publicador.notificar(evento());
        publicador.remover(obs);
        publicador.notificar(evento());

        assertEquals(1, recebidos.size());
    }

    @Test
    void falhaEmUmObservadorNaoImpedeOsDemais() {
        List<EventoTransacao> recebidos = new ArrayList<>();
        PublicadorTransacao publicador = new PublicadorTransacao(List.of());
        publicador.registrar(e -> { throw new IllegalStateException("quebrou"); });
        publicador.registrar(recebidos::add);

        assertDoesNotThrow(() -> publicador.notificar(evento()));
        assertEquals(1, recebidos.size());
    }
}
