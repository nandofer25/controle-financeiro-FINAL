package br.edu.fag.financeiro.orcamento.estado;

import br.edu.fag.financeiro.orcamento.Orcamento;
import br.edu.fag.financeiro.orcamento.OperacaoInvalidaException;
import br.edu.fag.financeiro.orcamento.StatusOrcamento;

import java.math.BigDecimal;
import java.util.Optional;

/** Mes fechado: o orcamento fica congelado (nao muda por novos lancamentos) ate ser reaberto. */
class EstadoEncerrado implements EstadoOrcamento {

    @Override
    public StatusOrcamento status() {
        return StatusOrcamento.ENCERRADO;
    }

    @Override
    public void atualizarConsumo(Orcamento orcamento, BigDecimal consumido, BigDecimal limite) {
        // congelado: ignora
    }

    @Override
    public void encerrar(Orcamento orcamento) {
        throw new OperacaoInvalidaException("Orcamento id=" + orcamento.getId() + " ja esta encerrado");
    }

    @Override
    public void reabrir(Orcamento orcamento) {
        // volta para NORMAL; o Service reavalia o consumo logo em seguida
        orcamento.mudarPara(StatusOrcamento.NORMAL);
    }

    @Override
    public void aoAlterarLimite(Orcamento orcamento) {
        throw new OperacaoInvalidaException("Orcamento id=" + orcamento.getId()
                + " esta encerrado; reabra antes de alterar o limite");
    }

    @Override
    public Optional<String> mensagemDeEntrada(Orcamento o, BigDecimal consumido, BigDecimal limite) {
        return Optional.empty();
    }
}
