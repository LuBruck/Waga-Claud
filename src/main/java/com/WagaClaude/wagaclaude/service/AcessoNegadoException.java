package com.WagaClaude.wagaclaude.service;

/**
 * Lançada quando um usuário tenta executar uma ação que seu nível de acesso
 * não permite (ex: um usuário COMUM tentando deletar um recurso).
 *
 * O Controller traduz essa exceção em HTTP 403 (Forbidden), diferente da
 * {@link IllegalArgumentException} (dados inválidos) que vira 400 (Bad Request).
 */
public class AcessoNegadoException extends RuntimeException {
    public AcessoNegadoException(String mensagem) {
        super(mensagem);
    }
}
