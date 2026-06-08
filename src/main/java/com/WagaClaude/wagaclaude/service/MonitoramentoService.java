package com.WagaClaude.wagaclaude.service;

import com.WagaClaude.wagaclaude.model.Monitoramento;
import com.WagaClaude.wagaclaude.model.Recurso;
import com.WagaClaude.wagaclaude.repository.MonitoramentoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Random;

@Service
public class MonitoramentoService {

    @Autowired
    private MonitoramentoRepository monitoramentoRepository;

    private static final Random random = new Random();

    private static final double LIMIAR_CRITICO = 95.0;

    public void gerarMetricasFake(Recurso recurso) {
        int quantidade = 5;

        for (int i = 0; i < quantidade; i++) {
            Monitoramento cpu = new Monitoramento();
            cpu.setRecurso(recurso);
            cpu.setMetrica("CPU");
            cpu.setValor(10 + random.nextDouble() * 89);
            monitoramentoRepository.save(cpu);

            Monitoramento ram = new Monitoramento();
            ram.setRecurso(recurso);
            ram.setMetrica("RAM");
            ram.setValor(20 + random.nextDouble() * 79); 
            monitoramentoRepository.save(ram);
        }
    }

    public List<Monitoramento> listarPorRecurso(Integer recursoId) {
        return monitoramentoRepository.findByRecursoId(recursoId);
    }

    public List<Monitoramento> listarCriticos() {
        return monitoramentoRepository.findByValorGreaterThan(LIMIAR_CRITICO);
    }
}
