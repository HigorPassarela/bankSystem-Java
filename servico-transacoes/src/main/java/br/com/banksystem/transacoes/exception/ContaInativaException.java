package br.com.banksystem.transacoes.exception;

/** Exceção lançada quando a conta está inativa. */
public class ContaInativaException extends RuntimeException {
    public ContaInativaException(String mensagem) { super(mensagem); }
}
