package br.edu.fag.financeiro.evento;

import br.edu.fag.financeiro.logger.AppLogger;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Implementacao manual do Subject (sem ApplicationEventPublisher/@EventListener).
 * O Spring so injeta a lista inicial de observadores; adicionar um novo comportamento
 * = criar um @Component que implementa ObservadorTransacao, sem alterar o Service.
 *
 * Falha em um observador NAO derruba a operacao principal nem os demais observadores:
 * o CRUD continua funcionando.
 */
@Component
public class PublicadorTransacao implements SujeitoTransacao {

    private final AppLogger log = AppLogger.getInstance();
    private final List<ObservadorTransacao> observadores = new CopyOnWriteArrayList<>();

    public PublicadorTransacao(List<ObservadorTransacao> iniciais) {
        iniciais.forEach(this::registrar);
    }

    @Override
    public void registrar(ObservadorTransacao observador) {
        observadores.add(observador);
        log.debug(getClass(), "[Observer] observador registrado: " + observador.getClass().getSimpleName());
    }

    @Override
    public void remover(ObservadorTransacao observador) {
        observadores.remove(observador);
        log.debug(getClass(), "[Observer] observador removido: " + observador.getClass().getSimpleName());
    }

    @Override
    public void notificar(EventoTransacao evento) {
        log.debug(getClass(), "[Observer] evento " + evento.tipo() + " (transacao id=" + evento.atual().id()
                + ") para " + observadores.size() + " observador(es)");
        for (ObservadorTransacao observador : observadores) {
            try {
                observador.aoOcorrer(evento);
            } catch (RuntimeException e) {
                log.erro(getClass(), "[Observer] falha em " + observador.getClass().getSimpleName(), e);
            }
        }
    }
}
