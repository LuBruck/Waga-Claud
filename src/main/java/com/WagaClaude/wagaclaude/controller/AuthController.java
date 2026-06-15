package com.WagaClaude.wagaclaude.controller;

import com.WagaClaude.wagaclaude.model.Usuario;
import com.WagaClaude.wagaclaude.service.AutenticacaoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class AuthController {

    @Autowired
    private AutenticacaoService autenticacaoService;

    /**
     * POST /api/login
     * Body: { "email": "...", "senha": "..." }
     * Retorna o usuário autenticado (200) ou 401 se as credenciais forem inválidas.
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> credenciais) {
        try {
            Usuario usuario = autenticacaoService.login(
                    credenciais.get("email"),
                    credenciais.get("senha"));
            return ResponseEntity.ok(usuario);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        }
    }

    /**
     * POST /api/cadastro
     * Body: objeto Usuario (nome, email, senha, nivelAcesso opcional).
     * Retorna o usuário criado (201) ou 400 se algo estiver inválido.
     */
    @PostMapping("/cadastro")
    public ResponseEntity<?> cadastro(@RequestBody Usuario usuario) {
        try {
            Usuario novo = autenticacaoService.cadastrar(usuario);
            return ResponseEntity.status(HttpStatus.CREATED).body(novo);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
