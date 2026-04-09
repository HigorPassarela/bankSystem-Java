package br.com.banksystem.contas.model;

import br.com.banksystem.contas.model.dto.StatusConta;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;
import java.time.LocalDateTime;

/**
 * Entidade que representa uma conta bancária no sistema.
 *
 * Ciclo de vida:
 *   PENDENTE_EMAIL → (verificação) → ATIVA → (encerramento) → ENCERRADA
 */
@Document(collection = "contas")
public class Conta {

    @Id private String id;
    @Indexed(unique = true) private String numeroConta;
    private String nomeCompleto;
    private String cpf;
    private String email;
    @Indexed(unique = true) private String telefone;
    private String senhaHash;
    /** PIN de 4 dígitos — exclusivo para autorizar transferências */
    private String senhaTransferenciaHash;
    private String role;

    /** Status atual da conta — reflete o ciclo de vida completo */
    private StatusConta status;

    /** Mantido por compatibilidade e conveniência — derivado de status */
    private Boolean ativa;

    private Boolean emailVerificado;
    private String tokenVerificacaoEmail;
    private LocalDateTime tokenVerificacaoExpiracao;
    private LocalDateTime dataCriacao;
    private LocalDateTime dataAtualizacao;

    public Conta() {}

    // ── Getters e Setters ────────────────────────────────────
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getNumeroConta() { return numeroConta; }
    public void setNumeroConta(String numeroConta) { this.numeroConta = numeroConta; }
    public String getNomeCompleto() { return nomeCompleto; }
    public void setNomeCompleto(String nomeCompleto) { this.nomeCompleto = nomeCompleto; }
    public String getCpf() { return cpf; }
    public void setCpf(String cpf) { this.cpf = cpf; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getTelefone() { return telefone; }
    public void setTelefone(String telefone) { this.telefone = telefone; }
    public String getSenhaHash() { return senhaHash; }
    public void setSenhaHash(String senhaHash) { this.senhaHash = senhaHash; }
    public String getSenhaTransferenciaHash() { return senhaTransferenciaHash; }
    public void setSenhaTransferenciaHash(String h) { this.senhaTransferenciaHash = h; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public StatusConta getStatus() { return status; }
    public void setStatus(StatusConta status) {
        this.status = status;
        this.ativa = (status == StatusConta.ATIVA);
    }
    public Boolean getAtiva() { return ativa; }
    public void setAtiva(Boolean ativa) { this.ativa = ativa; }
    public Boolean getEmailVerificado() { return emailVerificado; }
    public void setEmailVerificado(Boolean emailVerificado) { this.emailVerificado = emailVerificado; }
    public String getTokenVerificacaoEmail() { return tokenVerificacaoEmail; }
    public void setTokenVerificacaoEmail(String t) { this.tokenVerificacaoEmail = t; }
    public LocalDateTime getTokenVerificacaoExpiracao() { return tokenVerificacaoExpiracao; }
    public void setTokenVerificacaoExpiracao(LocalDateTime t) { this.tokenVerificacaoExpiracao = t; }
    public LocalDateTime getDataCriacao() { return dataCriacao; }
    public void setDataCriacao(LocalDateTime dataCriacao) { this.dataCriacao = dataCriacao; }
    public LocalDateTime getDataAtualizacao() { return dataAtualizacao; }
    public void setDataAtualizacao(LocalDateTime dataAtualizacao) { this.dataAtualizacao = dataAtualizacao; }
}
