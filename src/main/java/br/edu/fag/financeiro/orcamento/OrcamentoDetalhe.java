package br.edu.fag.financeiro.orcamento;

import java.math.BigDecimal;

/**
 * Visao de um orcamento para a API: dados cadastrados + consumo calculado no momento da consulta.
 * percentualConsumido e null quando nao ha limite calculavel (limite percentual e receitas do mes = 0).
 */
public record OrcamentoDetalhe(Long id,
                               String categoria,
                               int ano,
                               int mes,
                               TipoLimite tipoLimite,
                               BigDecimal valorLimite,
                               BigDecimal limiteEfetivo,
                               BigDecimal consumido,
                               BigDecimal restante,
                               BigDecimal percentualConsumido,
                               StatusOrcamento status) {
}
