package com.WagaClaude.wagaclaude.service;

import com.WagaClaude.wagaclaude.model.Usuario;
import com.WagaClaude.wagaclaude.model.enums.NivelAcesso;
import com.WagaClaude.wagaclaude.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class AutenticacaoService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    // Ações que só o ADMIN pode executar. Usado por temPermissao().
    private static final Set<String> ACOES_ADMIN = Set.of(
            "DELETAR_RECURSO",
            "VER_TODOS_RECURSOS",
            "GERENCIAR_USUARIOS"
    );

    /**
     * Valida email + senha. Retorna o usuário autenticado.
     * Lança IllegalArgumentException se as credenciais não baterem.
     */
    public Usuario login(String email, String senha) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Email ou senha inválidos"));

        if (!usuario.getSenha().equals(senha)) {
            throw new IllegalArgumentException("Email ou senha inválidos");
        }

        return usuario;
    }

    /**
     * Cadastra um novo usuário após validações básicas.
     * Lança IllegalArgumentException se algo estiver inválido ou o email já existir.
     */
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

        // Por padrão, todo cadastro novo é usuário comum.
        if (usuario.getNivelAcesso() == null) {
            usuario.setNivelAcesso(NivelAcesso.COMUM);
        }

        return usuarioRepository.save(usuario);
    }

    /**
     * Verifica se o usuário tem permissão para executar a ação informada.
     * ADMIN pode tudo; COMUM pode tudo menos as ações restritas a ADMIN.
     */
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
