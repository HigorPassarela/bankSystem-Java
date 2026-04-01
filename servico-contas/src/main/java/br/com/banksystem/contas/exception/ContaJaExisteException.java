package br.com.banksystem.contas.exception;

/**
 * Exceção lançada quando há tentativa de criar conta com dados já existentes.
 */
public class ContaJaExisteException extends RuntimeException {

    public ContaJaExisteException(String mensagem) {
        super(mensagem);
    }
}
