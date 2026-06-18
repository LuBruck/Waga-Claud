package com.WagaCloud.wagacloud.service;

import com.WagaCloud.wagacloud.model.Usuario;
import com.WagaCloud.wagacloud.model.enums.NivelAcesso;
import com.WagaCloud.wagacloud.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class AutenticacaoService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    private static final Set<String> ACOES_ADMIN = Set.of(
            "DELETAR_RECURSO",
            "VER_TODOS_RECURSOS",
            "GERENCIAR_USUARIOS"
    );

    public Usuario login(String email, String senha) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Email ou senha inválidos"));

        if (!usuario.getSenha().equals(senha)) {
            throw new IllegalArgumentException("Email ou senha inválidos");
        }

        return usuario;
    }

    public Usuario cadastrar(Usuario usuario) {
        if (usuario.getNome() == null || usuario.getNome().isBlank()) {
            throw new IllegalArgumentException("Nome é obrigatório");
        }
        if (usuario.getEmail() == null || !usuario.getEmail().contains("@")) {
            throw new IllegalArgumentException("Email inválido");
        }
        if (usuario.getSenha() == null || usuario.getSenha().isBlank()) {
            throw new IllegalArgumentException("Senha é obrigatória");
        }
        if (usuarioRepository.findByEmail(usuario.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Já existe um usuário com esse email");
        }

        if (usuario.getNivelAcesso() == null) {
            usuario.setNivelAcesso(NivelAcesso.COMUM);
        }

        return usuarioRepository.save(usuario);
    }

    public boolean temPermissao(Usuario usuario, String acao) {
        if (usuario == null) {
            return false;
        }
        if (usuario.getNivelAcesso() == NivelAcesso.ADMIN) {
            return true;
        }
        return !ACOES_ADMIN.contains(acao);
    }
}
