package br.com.banksystem.contas.model.enums;

/**
 * Status do ciclo de vida de uma conta bancária.
 * <p>
 * PENDENTE_EMAIL  → conta criada, aguardando verificação de e-mail
 * ATIVA           → e-mail verificado, conta operacional
 * SUSPENSA        → bloqueada por suspeita de fraude ou inadimplência
 * ENCERRADA       → conta encerrada pelo titular
 */
public enum StatusConta {
    PENDENTE_EMAIL,
    ATIVA,
    SUSPENSA,
    ENCERRADA
}
