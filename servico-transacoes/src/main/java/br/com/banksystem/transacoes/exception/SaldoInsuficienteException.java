package br.com.banksystem.transacoes.exception;

/** Exceção lançada quando o saldo é insuficiente para a transação. */
public class SaldoInsuficienteException extends RuntimeException {
    public SaldoInsuficienteException(String mensagem) { super(mensagem); }
}
