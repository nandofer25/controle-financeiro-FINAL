package br.edu.fag.financeiro.evento;

/** SUBJECT do Observer: mantem a lista de observadores e os notifica. */
public interface SujeitoTransacao {

    void registrar(ObservadorTransacao observador);

    void remover(ObservadorTransacao observador);

    void notificar(EventoTransacao evento);
}
