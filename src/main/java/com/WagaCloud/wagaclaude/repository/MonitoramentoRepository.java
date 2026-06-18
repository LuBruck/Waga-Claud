package com.WagaCloud.wagaclaude.repository;

import com.WagaCloud.wagaclaude.model.Monitoramento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MonitoramentoRepository extends JpaRepository<Monitoramento, Integer> {

    List<Monitoramento> findByRecursoId(Integer recursoId);

    List<Monitoramento> findByValorGreaterThan(double valor);
}
