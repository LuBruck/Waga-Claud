package com.WagaClaude.wagaclaude.controller;

import com.WagaClaude.wagaclaude.model.Armazenamento;
import com.WagaClaude.wagaclaude.model.Recurso;
import com.WagaClaude.wagaclaude.model.VirtualMachine;
import com.WagaClaude.wagaclaude.service.AcessoNegadoException;
import com.WagaClaude.wagaclaude.service.RecursoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class RecursoController {

    @Autowired
    private RecursoService recursoService;

    /**
     * GET /api/recursos?usuarioId=1 — lista as VMs e discos do usuário.
     */
    @GetMapping("/recursos")
    public ResponseEntity<?> listarRecursos(@RequestParam Integer usuarioId) {
        try {
            List<Recurso> recursos = recursoService.listarRecursos(usuarioId);
            return ResponseEntity.ok(recursos);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * POST /api/vms?usuarioId=1 — cria uma VM (com disco padrão).
     */
    @PostMapping("/vms")
    public ResponseEntity<?> criarVM(@RequestParam Integer usuarioId,
                                     @RequestBody VirtualMachine vm) {
        try {
            VirtualMachine criada = recursoService.criarVM(usuarioId, vm);
            return ResponseEntity.status(HttpStatus.CREATED).body(criada);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * POST /api/storages?usuarioId=1 — cria um disco avulso.
     */
    @PostMapping("/storages")
    public ResponseEntity<?> criarStorage(@RequestParam Integer usuarioId,
                                          @RequestBody Armazenamento disco) {
        try {
            Armazenamento criado = recursoService.criarStorage(usuarioId, disco);
            return ResponseEntity.status(HttpStatus.CREATED).body(criado);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * PUT /api/vms/{id}/iniciar — liga a VM (status ATIVO).
     */
    @PutMapping("/vms/{id}/iniciar")
    public ResponseEntity<?> iniciarVM(@PathVariable Integer id) {
        try {
            return ResponseEntity.ok(recursoService.iniciarVM(id));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * PUT /api/vms/{id}/parar — para a VM (status PARADO).
     */
    @PutMapping("/vms/{id}/parar")
    public ResponseEntity<?> pararVM(@PathVariable Integer id) {
        try {
            return ResponseEntity.ok(recursoService.pararVM(id));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * PUT /api/discos/{id}/expandir — body: { "gb": 50 }
     */
    @PutMapping("/discos/{id}/expandir")
    public ResponseEntity<?> expandirDisco(@PathVariable Integer id,
                                           @RequestBody Map<String, Integer> body) {
        try {
            return ResponseEntity.ok(recursoService.expandirDisco(id, body.get("gb")));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * PUT /api/discos/{id}/reduzir — body: { "gb": 50 }
     */
    @PutMapping("/discos/{id}/reduzir")
    public ResponseEntity<?> reduzirDisco(@PathVariable Integer id,
                                          @RequestBody Map<String, Integer> body) {
        try {
            return ResponseEntity.ok(recursoService.reduzirDisco(id, body.get("gb")));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * PUT /api/discos/anexar — body: { "discoId": 1, "vmId": 2 }
     */
    @PutMapping("/discos/anexar")
    public ResponseEntity<?> anexarDisco(@RequestBody Map<String, Integer> body) {
        try {
            Armazenamento disco = recursoService.anexarDisco(body.get("discoId"), body.get("vmId"));
            return ResponseEntity.ok(disco);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * PUT /api/discos/desanexar — body: { "discoId": 1 }
     */
    @PutMapping("/discos/desanexar")
    public ResponseEntity<?> desanexarDisco(@RequestBody Map<String, Integer> body) {
        try {
            Armazenamento disco = recursoService.desanexarDisco(body.get("discoId"));
            return ResponseEntity.ok(disco);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * DELETE /api/recursos/{id}?usuarioId=1 — deleta uma VM ou disco.
     * Apenas ADMIN tem permissão; usuário comum recebe 403.
     */
    @DeleteMapping("/recursos/{id}")
    public ResponseEntity<?> deletarRecurso(@PathVariable Integer id,
                                            @RequestParam Integer usuarioId) {
        try {
            recursoService.deletarRecurso(id, usuarioId);
            return ResponseEntity.noContent().build();
        } catch (AcessoNegadoException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
