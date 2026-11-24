package br.edu.fag.financeiro.evento;

/**
 * Evento publicado quando uma transacao muda. {@code anterior} so existe em ATUALIZADA
 * (permite reavaliar tambem a categoria/mes antigos quando o lancamento "muda de lugar").
 */
public record EventoTransacao(TipoEvento tipo, DadosTransacao atual, DadosTransacao anterior) {
}
