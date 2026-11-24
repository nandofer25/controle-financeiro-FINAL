package br.edu.fag.financeiro.evento;

import br.edu.fag.financeiro.model.TipoTransacao;
import br.edu.fag.financeiro.model.Transacao;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Copia imutavel dos dados de uma Transacao no momento do evento (os observadores nao recebem a entidade JPA). */
public record DadosTransacao(Long id, TipoTransacao tipo, BigDecimal valor, String categoria, LocalDate data) {

    public static DadosTransacao de(Transacao t) {
        return new DadosTransacao(t.getId(), t.getTipo(), t.getValor(), t.getCategoria(), t.getData());
    }
}
