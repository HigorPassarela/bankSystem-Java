package br.com.banksystem.transacoes.exception;

/** Exceção lançada quando a transferência não pode ser realizada. */
public class TransferenciaInvalidaException extends RuntimeException {
    public TransferenciaInvalidaException(String mensagem) { super(mensagem); }
}
