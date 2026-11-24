package br.edu.fag.financeiro.orcamento;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/** Corpo do POST /orcamentos. */
public record OrcamentoRequest(
        @NotBlank(message = "categoria e obrigatoria")
        @Size(max = 60, message = "categoria aceita no maximo 60 caracteres")
        @Pattern(regexp = "[^\\p{Cntrl}]*", message = "categoria nao pode ter caracteres de controle")
        String categoria,
        @NotNull(message = "ano e obrigatorio") @Min(value = 2000, message = "ano invalido") @Max(value = 2100, message = "ano invalido") Integer ano,
        @NotNull(message = "mes e obrigatorio") @Min(value = 1, message = "mes deve ser de 1 a 12") @Max(value = 12, message = "mes deve ser de 1 a 12") Integer mes,
        @NotNull(message = "tipoLimite e obrigatorio (FIXO ou PERCENTUAL_DA_RECEITA)") TipoLimite tipoLimite,
        @NotNull(message = "valorLimite e obrigatorio") @Positive(message = "valorLimite deve ser maior que zero") BigDecimal valorLimite) {
}
