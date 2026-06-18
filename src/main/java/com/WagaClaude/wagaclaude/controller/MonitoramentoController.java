package com.WagaClaude.wagaclaude.controller;

import com.WagaClaude.wagaclaude.model.Monitoramento;
import com.WagaClaude.wagaclaude.service.MonitoramentoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/monitoramento")
public class MonitoramentoController {

    @Autowired
    private MonitoramentoService monitoramentoService;

    @GetMapping("/{recursoId}")
    public ResponseEntity<List<Monitoramento>> listarPorRecurso(@PathVariable Integer recursoId) {
        List<Monitoramento> metricas = monitoramentoService.listarPorRecurso(recursoId);
        return ResponseEntity.ok(metricas);
    }

    @PostMapping("/gerar")
    public ResponseEntity<String> gerarMetricas(@RequestParam Integer recursoId) {
        try {
            monitoramentoService.gerarMetricasFakePorId(recursoId);
            return ResponseEntity.ok("Métricas geradas para o recurso " + recursoId);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/criticos")
    public ResponseEntity<List<Monitoramento>> listarCriticos() {
        List<Monitoramento> criticos = monitoramentoService.listarCriticos();
        return ResponseEntity.ok(criticos);
    }
}