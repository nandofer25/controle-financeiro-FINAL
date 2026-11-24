package br.edu.fag.financeiro.orcamento;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrcamentoRepository extends JpaRepository<Orcamento, Long> {

    Optional<Orcamento> findByCategoriaIgnoreCaseAndAnoAndMes(String categoria, int ano, int mes);

    List<Orcamento> findByAnoAndMes(int ano, int mes);
}
