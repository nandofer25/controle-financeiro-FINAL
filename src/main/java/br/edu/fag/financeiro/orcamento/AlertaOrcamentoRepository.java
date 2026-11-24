package br.edu.fag.financeiro.orcamento;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AlertaOrcamentoRepository extends JpaRepository<AlertaOrcamento, Long> {

    List<AlertaOrcamento> findAllByOrderByIdDesc();
}
