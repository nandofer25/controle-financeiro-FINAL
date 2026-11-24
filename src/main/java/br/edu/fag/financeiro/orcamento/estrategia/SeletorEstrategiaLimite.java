package br.edu.fag.financeiro.orcamento.estrategia;

import br.edu.fag.financeiro.orcamento.TipoLimite;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Escolhe a Strategy pelo tipo, sem if/switch. O Spring injeta TODAS as implementacoes
 * de EstrategiaLimite; criar uma nova estrategia nao exige mexer aqui nem no Service.
 */
@Component
public class SeletorEstrategiaLimite {

    private final Map<TipoLimite, EstrategiaLimite> estrategias = new EnumMap<>(TipoLimite.class);

    public SeletorEstrategiaLimite(List<EstrategiaLimite> implementacoes) {
        implementacoes.forEach(e -> estrategias.put(e.tipo(), e));
    }

    public EstrategiaLimite para(TipoLimite tipo) {
        EstrategiaLimite estrategia = estrategias.get(tipo);
        if (estrategia == null) {
            throw new IllegalStateException("Nenhuma estrategia de limite cadastrada para " + tipo);
        }
        return estrategia;
    }
}
