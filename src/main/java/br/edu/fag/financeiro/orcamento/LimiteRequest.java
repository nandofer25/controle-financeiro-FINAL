package br.edu.fag.financeiro.orcamento;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

/** Corpo do PUT /orcamentos/{id}/limite. */
public record LimiteRequest(
        @NotNull(message = "tipoLimite e obrigatorio (FIXO ou PERCENTUAL_DA_RECEITA)") TipoLimite tipoLimite,
        @NotNull(message = "valorLimite e obrigatorio") @Positive(message = "valorLimite deve ser maior que zero") BigDecimal valorLimite) {
}
