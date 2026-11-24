package br.edu.fag.financeiro.orcamento.estado;

import br.edu.fag.financeiro.orcamento.StatusOrcamento;

import java.util.EnumMap;
import java.util.Map;

/** Converte o status persistido (enum) no objeto de estado correspondente. Os estados nao tem dados, entao sao compartilhados. */
public final class EstadoOrcamentoFactory {

    private static final Map<StatusOrcamento, EstadoOrcamento> ESTADOS = new EnumMap<>(StatusOrcamento.class);

    static {
        ESTADOS.put(StatusOrcamento.NORMAL, new EstadoNormal());
        ESTADOS.put(StatusOrcamento.ATENCAO, new EstadoAtencao());
        ESTADOS.put(StatusOrcamento.ESTOURADO, new EstadoEstourado());
        ESTADOS.put(StatusOrcamento.ENCERRADO, new EstadoEncerrado());
    }

    private EstadoOrcamentoFactory() {
    }

    public static EstadoOrcamento de(StatusOrcamento status) {
        return ESTADOS.get(status);
    }
}
