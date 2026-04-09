package br.com.banksystem.fraudes.kafka;

import br.com.banksystem.fraudes.dto.TransacaoEventoDTO;
import br.com.banksystem.fraudes.service.AnaliseFraudeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes do FraudeConsumer")
class FraudeConsumerTest {

    @Mock
    private AnaliseFraudeService analiseFraudeService;

    private FraudeConsumer fraudeConsumer;

    @BeforeEach
    void setUp() {
        fraudeConsumer = new FraudeConsumer(analiseFraudeService);
    }

    @Test
    @DisplayName("Deve consumir transação aprovada e iniciar análise de fraude")
    void deveConsumirTransacaoAprovadaEIniciarAnaliseFraude() {
        // Given
        TransacaoEventoDTO evento = new TransacaoEventoDTO(
                "tx-001",
                "12345-6",
                new BigDecimal("1500.00"),
                "TRANSFERENCIA",
                "APROVADA",
                "Transferência realizada",
                LocalDateTime.now(),
                new BigDecimal("8500.00"),
                "12345-6",
                "99999-9"
        );

        // When
        fraudeConsumer.consumirTransacaoAprovada(evento);

        // Then
        verify(analiseFraudeService).iniciarAnalise(evento);
    }

    @Test
    @DisplayName("Deve consumir transação aprovada com campos nulos e iniciar análise")
    void deveConsumirTransacaoAprovadaComCamposNulosEIniciarAnalise() {
        // Given
        TransacaoEventoDTO evento = new TransacaoEventoDTO(
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );

        // When
        fraudeConsumer.consumirTransacaoAprovada(evento);

        // Then
        verify(analiseFraudeService).iniciarAnalise(evento);
    }
}