package br.com.banksystem.extratos.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Testes da entidade Transacao")
class TransacaoTest {

    @Test
    @DisplayName("Deve criar transação com construtor padrão")
    void deveCriarTransacaoComConstrutorPadrao() {
        // When
        Transacao transacao = new Transacao();

        // Then
        assertThat(transacao).isNotNull();
        assertThat(transacao.getId()).isNull();
        assertThat(transacao.getNumeroConta()).isNull();
        assertThat(transacao.getIdTransacao()).isNull();
        assertThat(transacao.getValor()).isNull();
        assertThat(transacao.getTipo()).isNull();
        assertThat(transacao.getStatus()).isNull();
        assertThat(transacao.getDescricao()).isNull();
        assertThat(transacao.getDataHora()).isNull();
        assertThat(transacao.getSaldoAposTransacao()).isNull();
    }

    @Test
    @DisplayName("Deve definir e obter id corretamente")
    void deveDefinirEObterIdCorretamente() {
        // Given
        Transacao transacao = new Transacao();
        String id = "507f1f77bcf86cd799439011";

        // When
        transacao.setId(id);

        // Then
        assertThat(transacao.getId()).isEqualTo(id);
    }

    @Test
    @DisplayName("Deve definir e obter numeroConta corretamente")
    void deveDefinirEObterNumeroContaCorretamente() {
        // Given
        Transacao transacao = new Transacao();
        String numeroConta = "12345-6";

        // When
        transacao.setNumeroConta(numeroConta);

        // Then
        assertThat(transacao.getNumeroConta()).isEqualTo(numeroConta);
    }

    @Test
    @DisplayName("Deve definir e obter idTransacao corretamente")
    void deveDefinirEObterIdTransacaoCorretamente() {
        // Given
        Transacao transacao = new Transacao();
        String idTransacao = "tx-123";

        // When
        transacao.setIdTransacao(idTransacao);

        // Then
        assertThat(transacao.getIdTransacao()).isEqualTo(idTransacao);
    }

    @Test
    @DisplayName("Deve definir e obter valor corretamente")
    void deveDefinirEObterValorCorretamente() {
        // Given
        Transacao transacao = new Transacao();
        BigDecimal valor = new BigDecimal("150.75");

        // When
        transacao.setValor(valor);

        // Then
        assertThat(transacao.getValor()).isEqualTo(valor);
    }

    @Test
    @DisplayName("Deve definir e obter tipo corretamente")
    void deveDefinirEObterTipoCorretamente() {
        // Given
        Transacao transacao = new Transacao();
        String tipo = "DEPOSITO";

        // When
        transacao.setTipo(tipo);

        // Then
        assertThat(transacao.getTipo()).isEqualTo(tipo);
    }

    @Test
    @DisplayName("Deve definir e obter status corretamente")
    void deveDefinirEObterStatusCorretamente() {
        // Given
        Transacao transacao = new Transacao();
        String status = "APROVADA";

        // When
        transacao.setStatus(status);

        // Then
        assertThat(transacao.getStatus()).isEqualTo(status);
    }

    @Test
    @DisplayName("Deve definir e obter descricao corretamente")
    void deveDefinirEObterDescricaoCorretamente() {
        // Given
        Transacao transacao = new Transacao();
        String descricao = "Transferência recebida";

        // When
        transacao.setDescricao(descricao);

        // Then
        assertThat(transacao.getDescricao()).isEqualTo(descricao);
    }

    @Test
    @DisplayName("Deve definir e obter dataHora corretamente")
    void deveDefinirEObterDataHoraCorretamente() {
        // Given
        Transacao transacao = new Transacao();
        LocalDateTime dataHora = LocalDateTime.now();

        // When
        transacao.setDataHora(dataHora);

        // Then
        assertThat(transacao.getDataHora()).isEqualTo(dataHora);
    }

    @Test
    @DisplayName("Deve definir e obter saldoAposTransacao corretamente")
    void deveDefinirEObterSaldoAposTransacaoCorretamente() {
        // Given
        Transacao transacao = new Transacao();
        BigDecimal saldo = new BigDecimal("1250.00");

        // When
        transacao.setSaldoAposTransacao(saldo);

        // Then
        assertThat(transacao.getSaldoAposTransacao()).isEqualTo(saldo);
    }

    @Test
    @DisplayName("Deve preencher todos os campos corretamente")
    void devePreencherTodosOsCamposCorretamente() {
        // Given
        Transacao transacao = new Transacao();
        String id = "507f1f77bcf86cd799439011";
        String numeroConta = "12345-6";
        String idTransacao = "tx-001";
        BigDecimal valor = new BigDecimal("500.00");
        String tipo = "TRANSFERENCIA_ENTRADA";
        String status = "APROVADA";
        String descricao = "Transferência recebida";
        LocalDateTime dataHora = LocalDateTime.now();
        BigDecimal saldoAposTransacao = new BigDecimal("1500.00");

        // When
        transacao.setId(id);
        transacao.setNumeroConta(numeroConta);
        transacao.setIdTransacao(idTransacao);
        transacao.setValor(valor);
        transacao.setTipo(tipo);
        transacao.setStatus(status);
        transacao.setDescricao(descricao);
        transacao.setDataHora(dataHora);
        transacao.setSaldoAposTransacao(saldoAposTransacao);

        // Then
        assertThat(transacao.getId()).isEqualTo(id);
        assertThat(transacao.getNumeroConta()).isEqualTo(numeroConta);
        assertThat(transacao.getIdTransacao()).isEqualTo(idTransacao);
        assertThat(transacao.getValor()).isEqualTo(valor);
        assertThat(transacao.getTipo()).isEqualTo(tipo);
        assertThat(transacao.getStatus()).isEqualTo(status);
        assertThat(transacao.getDescricao()).isEqualTo(descricao);
        assertThat(transacao.getDataHora()).isEqualTo(dataHora);
        assertThat(transacao.getSaldoAposTransacao()).isEqualTo(saldoAposTransacao);
    }

    @Test
    @DisplayName("Deve aceitar valores nulos nos campos")
    void deveAceitarValoresNulosNosCampos() {
        // Given
        Transacao transacao = new Transacao();

        // When
        transacao.setId(null);
        transacao.setNumeroConta(null);
        transacao.setIdTransacao(null);
        transacao.setValor(null);
        transacao.setTipo(null);
        transacao.setStatus(null);
        transacao.setDescricao(null);
        transacao.setDataHora(null);
        transacao.setSaldoAposTransacao(null);

        // Then
        assertThat(transacao.getId()).isNull();
        assertThat(transacao.getNumeroConta()).isNull();
        assertThat(transacao.getIdTransacao()).isNull();
        assertThat(transacao.getValor()).isNull();
        assertThat(transacao.getTipo()).isNull();
        assertThat(transacao.getStatus()).isNull();
        assertThat(transacao.getDescricao()).isNull();
        assertThat(transacao.getDataHora()).isNull();
        assertThat(transacao.getSaldoAposTransacao()).isNull();
    }

    @Test
    @DisplayName("Deve possuir anotação Document com collection transacoes")
    void devePossuirAnotacaoDocument() {
        // Given
        Class<Transacao> clazz = Transacao.class;

        // When
        Document annotation = clazz.getAnnotation(Document.class);

        // Then
        assertThat(annotation).isNotNull();
        assertThat(annotation.collection()).isEqualTo("transacoes");
    }

    @Test
    @DisplayName("Deve possuir anotação CompoundIndexes")
    void devePossuirAnotacaoCompoundIndexes() {
        // Given
        Class<Transacao> clazz = Transacao.class;

        // When
        CompoundIndexes annotation = clazz.getAnnotation(CompoundIndexes.class);

        // Then
        assertThat(annotation).isNotNull();
        assertThat(annotation.value()).hasSize(2);
    }

    @Test
    @DisplayName("Deve manter precisão em valores monetários")
    void deveManterPrecisaoEmValoresMonetarios() {
        // Given
        Transacao transacao = new Transacao();
        BigDecimal valor = new BigDecimal("1234.56");
        BigDecimal saldo = new BigDecimal("7890.12");

        // When
        transacao.setValor(valor);
        transacao.setSaldoAposTransacao(saldo);

        // Then
        assertThat(transacao.getValor()).isEqualByComparingTo("1234.56");
        assertThat(transacao.getSaldoAposTransacao()).isEqualByComparingTo("7890.12");
    }

    @Test
    @DisplayName("Deve armazenar tipos de transação válidos")
    void deveArmazenarTiposDeTransacaoValidos() {
        // Given
        Transacao transacao = new Transacao();
        String[] tiposValidos = {
                "DEPOSITO",
                "DEBITO",
                "CREDITO",
                "TRANSFERENCIA_SAIDA",
                "TRANSFERENCIA_ENTRADA",
                "ESTORNO_FRAUDE"
        };

        // When / Then
        for (String tipo : tiposValidos) {
            transacao.setTipo(tipo);
            assertThat(transacao.getTipo()).isEqualTo(tipo);
        }
    }

    @Test
    @DisplayName("Deve armazenar status válidos")
    void deveArmazenarStatusValidos() {
        // Given
        Transacao transacao = new Transacao();

        // When
        transacao.setStatus("APROVADA");
        assertThat(transacao.getStatus()).isEqualTo("APROVADA");

        transacao.setStatus("REPROVADA");
        assertThat(transacao.getStatus()).isEqualTo("REPROVADA");
    }
}