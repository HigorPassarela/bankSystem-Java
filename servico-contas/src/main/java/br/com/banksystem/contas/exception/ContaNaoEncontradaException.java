package br.com.banksystem.contas.exception;

/**
 * Exceção lançada quando uma conta não é encontrada.
 */
public class ContaNaoEncontradaException extends RuntimeException {

    public ContaNaoEncontradaException(String mensagem) {
        super(mensagem);
    }

    public ContaNaoEncontradaException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }
}
