package br.edu.fag.financeiro.repository;

import br.edu.fag.financeiro.model.TipoTransacao;
import br.edu.fag.financeiro.model.Transacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface TransacaoRepository extends JpaRepository<Transacao, Long> {

    @Query("select coalesce(sum(t.valor), 0) from Transacao t where t.tipo = :tipo")
    BigDecimal somarPorTipo(@Param("tipo") TipoTransacao tipo);

    /** Soma de um tipo (RECEITA/DESPESA) em um periodo, de todas as categorias. */
    @Query("select coalesce(sum(t.valor), 0) from Transacao t "
            + "where t.tipo = :tipo and t.data between :inicio and :fim")
    BigDecimal somarPorTipoNoPeriodo(@Param("tipo") TipoTransacao tipo,
                                     @Param("inicio") LocalDate inicio,
                                     @Param("fim") LocalDate fim);

    /** Soma de um tipo em um periodo para UMA categoria (sem diferenciar maiusculas/minusculas). */
    @Query("select coalesce(sum(t.valor), 0) from Transacao t "
            + "where t.tipo = :tipo and lower(t.categoria) = lower(:categoria) and t.data between :inicio and :fim")
    BigDecimal somarPorTipoECategoriaNoPeriodo(@Param("tipo") TipoTransacao tipo,
                                               @Param("categoria") String categoria,
                                               @Param("inicio") LocalDate inicio,
                                               @Param("fim") LocalDate fim);
}
