package com.WagaCloud.wagacloud.controller;

import com.WagaCloud.wagacloud.model.Armazenamento;
import com.WagaCloud.wagacloud.model.Recurso;
import com.WagaCloud.wagacloud.model.VirtualMachine;
import com.WagaCloud.wagacloud.service.AcessoNegadoException;
import com.WagaCloud.wagacloud.service.RecursoService;
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

    @GetMapping("/recursos")
    public ResponseEntity<?> listarRecursos(@RequestParam Integer usuarioId) {
        try {
            List<Recurso> recursos = recursoService.listarRecursos(usuarioId);
            return ResponseEntity.ok(recursos);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

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

    @PutMapping("/vms/{id}/iniciar")
    public ResponseEntity<?> iniciarVM(@PathVariable Integer id) {
        try {
            return ResponseEntity.ok(recursoService.iniciarVM(id));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/vms/{id}/parar")
    public ResponseEntity<?> pararVM(@PathVariable Integer id) {
        try {
            return ResponseEntity.ok(recursoService.pararVM(id));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/discos/{id}/expandir")
    public ResponseEntity<?> expandirDisco(@PathVariable Integer id,
                                           @RequestBody Map<String, Integer> body) {
        try {
            return ResponseEntity.ok(recursoService.expandirDisco(id, body.get("gb")));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/discos/{id}/reduzir")
    public ResponseEntity<?> reduzirDisco(@PathVariable Integer id,
                                          @RequestBody Map<String, Integer> body) {
        try {
            return ResponseEntity.ok(recursoService.reduzirDisco(id, body.get("gb")));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/discos/anexar")
    public ResponseEntity<?> anexarDisco(@RequestBody Map<String, Integer> body) {
        try {
            Armazenamento disco = recursoService.anexarDisco(body.get("discoId"), body.get("vmId"));
            return ResponseEntity.ok(disco);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/discos/desanexar")
    public ResponseEntity<?> desanexarDisco(@RequestBody Map<String, Integer> body) {
        try {
            Armazenamento disco = recursoService.desanexarDisco(body.get("discoId"));
            return ResponseEntity.ok(disco);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

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
