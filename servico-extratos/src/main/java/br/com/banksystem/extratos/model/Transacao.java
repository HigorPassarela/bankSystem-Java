package br.com.banksystem.extratos.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Documento MongoDB que representa uma transação persistida no extrato.
 *
 * Tipos possíveis:
 * DEPOSITO | DEBITO | CREDITO | TRANSFERENCIA_SAIDA | TRANSFERENCIA_ENTRADA | ESTORNO_FRAUDE
 * Status possíveis: APROVADA | REPROVADA
 *
 * Índices compostos otimizam as consultas mais comuns:
 *  - extrato por conta (numeroConta + dataHora)
 *  - extrato por conta e período (numeroConta + dataHora + tipo)
 */
@Document(collection = "transacoes")
@CompoundIndexes({
    @CompoundIndex(name = "idx_conta_data",      def = "{\'numeroConta\': 1, \'dataHora\': -1}"),
    @CompoundIndex(name = "idx_conta_data_tipo", def = "{\'numeroConta\': 1, \'dataHora\': -1, \'tipo\': 1}")
})
public class Transacao {

    @Id
    private String id;

    /** Número da conta proprietária da linha de extrato */
    @Indexed
    private String numeroConta;

    /** ID único da transação (gerado pelo servico-transacoes) */
    @Indexed(unique = true)
    private String idTransacao;

    private BigDecimal valor;

    /**
     * Tipo da transação:
     * DEPOSITO | DEBITO | CREDITO | TRANSFERENCIA_SAIDA | TRANSFERENCIA_ENTRADA
     */
    private String tipo;

    /** APROVADA ou REPROVADA */
    private String status;

    private String descricao;

    @Indexed
    private LocalDateTime dataHora;

    /** Saldo disponível após a transação (informativo) */
    private BigDecimal saldoAposTransacao;

    public Transacao() {}

    // ── Getters e Setters ────────────────────────────────────
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getNumeroConta() { return numeroConta; }
    public void setNumeroConta(String numeroConta) { this.numeroConta = numeroConta; }
    public String getIdTransacao() { return idTransacao; }
    public void setIdTransacao(String idTransacao) { this.idTransacao = idTransacao; }
    public BigDecimal getValor() { return valor; }
    public void setValor(BigDecimal valor) { this.valor = valor; }
    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }
    public LocalDateTime getDataHora() { return dataHora; }
    public void setDataHora(LocalDateTime dataHora) { this.dataHora = dataHora; }
    public BigDecimal getSaldoAposTransacao() { return saldoAposTransacao; }
    public void setSaldoAposTransacao(BigDecimal saldoAposTransacao) { this.saldoAposTransacao = saldoAposTransacao; }
}
