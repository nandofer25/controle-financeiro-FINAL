package br.edu.fag.financeiro.evento;

/**
 * OBSERVER: quem quer saber que uma transacao mudou implementa esta interface.
 * O TransacaoService nao conhece nenhum observador concreto.
 */
public interface ObservadorTransacao {

    void aoOcorrer(EventoTransacao evento);
}
