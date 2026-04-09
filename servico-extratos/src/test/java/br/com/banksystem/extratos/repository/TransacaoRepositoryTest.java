package br.com.banksystem.extratos.repository;

import br.com.banksystem.extratos.model.Transacao;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes do TransacaoRepository")
class TransacaoRepositoryTest {

    @Mock
    private TransacaoRepository transacaoRepository;

    private Transacao transacao1;
    private Transacao transacao2;
    private Transacao transacao3;

    @BeforeEach
    void setUp() {
        transacao1 = criarTransacao(
                "id-1",
                "tx-001",
                "12345-6",
                new BigDecimal("100.00"),
                "DEPOSITO",
                "APROVADA",
                "Depósito realizado",
                LocalDateTime.now(),
                new BigDecimal("1100.00")
        );

        transacao2 = criarTransacao(
                "id-2",
                "tx-002",
                "12345-6",
                new BigDecimal("50.00"),
                "DEBITO",
                "APROVADA",
                "Débito realizado",
                LocalDateTime.now().minusHours(1),
                new BigDecimal("1050.00")
        );

        transacao3 = criarTransacao(
                "id-3",
                "tx-003",
                "99999-9",
                new BigDecimal("200.00"),
                "CREDITO",
                "REPROVADA",
                "Crédito reprovado",
                LocalDateTime.now().minusDays(1),
                new BigDecimal("500.00")
        );
    }

    @Test
    @DisplayName("Deve encontrar transações por número da conta ordenadas por data desc")
    void deveFindByNumeroContaOrderByDataHoraDesc() {
        // Given
        when(transacaoRepository.findByNumeroContaOrderByDataHoraDesc("12345-6"))
                .thenReturn(List.of(transacao1, transacao2));

        // When
        List<Transacao> resultado = transacaoRepository.findByNumeroContaOrderByDataHoraDesc("12345-6");

        // Then
        assertThat(resultado).hasSize(2);
        assertThat(resultado.get(0).getNumeroConta()).isEqualTo("12345-6");
        assertThat(resultado.get(0).getIdTransacao()).isEqualTo("tx-001");
        assertThat(resultado.get(1).getIdTransacao()).isEqualTo("tx-002");
        verify(transacaoRepository).findByNumeroContaOrderByDataHoraDesc("12345-6");
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando não encontrar transações por conta")
    void deveRetornarListaVaziaQuandoNaoEncontrarPorNumeroConta() {
        // Given
        when(transacaoRepository.findByNumeroContaOrderByDataHoraDesc("00000-0"))
                .thenReturn(List.of());

        // When
        List<Transacao> resultado = transacaoRepository.findByNumeroContaOrderByDataHoraDesc("00000-0");

        // Then
        assertThat(resultado).isEmpty();
        verify(transacaoRepository).findByNumeroContaOrderByDataHoraDesc("00000-0");
    }

    @Test
    @DisplayName("Deve encontrar transações paginadas por número da conta")
    void deveFindByNumeroContaOrderByDataHoraDescComPaginacao() {
        // Given
        Pageable pageable = PageRequest.of(0, 2, Sort.by(Sort.Direction.DESC, "dataHora"));
        Page<Transacao> pagina = new PageImpl<>(List.of(transacao1, transacao2), pageable, 2);

        when(transacaoRepository.findByNumeroContaOrderByDataHoraDesc("12345-6", pageable))
                .thenReturn(pagina);

        // When
        Page<Transacao> resultado = transacaoRepository.findByNumeroContaOrderByDataHoraDesc("12345-6", pageable);

        // Then
        assertThat(resultado.getContent()).hasSize(2);
        assertThat(resultado.getTotalElements()).isEqualTo(2);
        assertThat(resultado.getContent().get(0).getIdTransacao()).isEqualTo("tx-001");
        verify(transacaoRepository).findByNumeroContaOrderByDataHoraDesc("12345-6", pageable);
    }

    @Test
    @DisplayName("Deve encontrar transações por conta e período")
    void deveFindByNumeroContaAndDataHoraBetweenOrderByDataHoraDesc() {
        // Given
        LocalDateTime inicio = LocalDateTime.now().minusDays(1);
        LocalDateTime fim = LocalDateTime.now();

        when(transacaoRepository.findByNumeroContaAndDataHoraBetweenOrderByDataHoraDesc("12345-6", inicio, fim))
                .thenReturn(List.of(transacao1, transacao2));

        // When
        List<Transacao> resultado =
                transacaoRepository.findByNumeroContaAndDataHoraBetweenOrderByDataHoraDesc("12345-6", inicio, fim);

        // Then
        assertThat(resultado).hasSize(2);
        assertThat(resultado).extracting(Transacao::getNumeroConta).containsOnly("12345-6");
        verify(transacaoRepository)
                .findByNumeroContaAndDataHoraBetweenOrderByDataHoraDesc("12345-6", inicio, fim);
    }

    @Test
    @DisplayName("Deve encontrar transações por conta e tipo")
    void deveFindByNumeroContaAndTipoOrderByDataHoraDesc() {
        // Given
        when(transacaoRepository.findByNumeroContaAndTipoOrderByDataHoraDesc("12345-6", "DEPOSITO"))
                .thenReturn(List.of(transacao1));

        // When
        List<Transacao> resultado =
                transacaoRepository.findByNumeroContaAndTipoOrderByDataHoraDesc("12345-6", "DEPOSITO");

        // Then
        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getTipo()).isEqualTo("DEPOSITO");
        verify(transacaoRepository).findByNumeroContaAndTipoOrderByDataHoraDesc("12345-6", "DEPOSITO");
    }

    @Test
    @DisplayName("Deve encontrar transações por conta, tipo e período")
    void deveFindByNumeroContaAndTipoAndDataHoraBetweenOrderByDataHoraDesc() {
        // Given
        LocalDateTime inicio = LocalDateTime.now().minusDays(1);
        LocalDateTime fim = LocalDateTime.now();

        when(transacaoRepository.findByNumeroContaAndTipoAndDataHoraBetweenOrderByDataHoraDesc(
                "12345-6", "DEPOSITO", inicio, fim))
                .thenReturn(List.of(transacao1));

        // When
        List<Transacao> resultado =
                transacaoRepository.findByNumeroContaAndTipoAndDataHoraBetweenOrderByDataHoraDesc(
                        "12345-6", "DEPOSITO", inicio, fim);

        // Then
        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getNumeroConta()).isEqualTo("12345-6");
        assertThat(resultado.get(0).getTipo()).isEqualTo("DEPOSITO");
        verify(transacaoRepository)
                .findByNumeroContaAndTipoAndDataHoraBetweenOrderByDataHoraDesc("12345-6", "DEPOSITO", inicio, fim);
    }

    @Test
    @DisplayName("Deve encontrar transação por idTransacao")
    void deveFindByIdTransacao() {
        // Given
        when(transacaoRepository.findByIdTransacao("tx-001"))
                .thenReturn(Optional.of(transacao1));

        // When
        Optional<Transacao> resultado = transacaoRepository.findByIdTransacao("tx-001");

        // Then
        assertThat(resultado).isPresent();
        assertThat(resultado.get().getIdTransacao()).isEqualTo("tx-001");
        verify(transacaoRepository).findByIdTransacao("tx-001");
    }

    @Test
    @DisplayName("Deve retornar vazio quando não encontrar por idTransacao")
    void deveRetornarVazioQuandoNaoEncontrarPorIdTransacao() {
        // Given
        when(transacaoRepository.findByIdTransacao("tx-inexistente"))
                .thenReturn(Optional.empty());

        // When
        Optional<Transacao> resultado = transacaoRepository.findByIdTransacao("tx-inexistente");

        // Then
        assertThat(resultado).isEmpty();
        verify(transacaoRepository).findByIdTransacao("tx-inexistente");
    }

    @Test
    @DisplayName("Deve verificar existência por idTransacao")
    void deveExistsByIdTransacao() {
        // Given
        when(transacaoRepository.existsByIdTransacao("tx-001")).thenReturn(true);
        when(transacaoRepository.existsByIdTransacao("tx-inexistente")).thenReturn(false);

        // When / Then
        assertThat(transacaoRepository.existsByIdTransacao("tx-001")).isTrue();
        assertThat(transacaoRepository.existsByIdTransacao("tx-inexistente")).isFalse();

        verify(transacaoRepository).existsByIdTransacao("tx-001");
        verify(transacaoRepository).existsByIdTransacao("tx-inexistente");
    }

    @Test
    @DisplayName("Deve contar transações por número da conta e status")
    void deveCountByNumeroContaAndStatus() {
        // Given
        when(transacaoRepository.countByNumeroContaAndStatus("12345-6", "APROVADA")).thenReturn(2L);
        when(transacaoRepository.countByNumeroContaAndStatus("99999-9", "REPROVADA")).thenReturn(1L);

        // When / Then
        assertThat(transacaoRepository.countByNumeroContaAndStatus("12345-6", "APROVADA")).isEqualTo(2L);
        assertThat(transacaoRepository.countByNumeroContaAndStatus("99999-9", "REPROVADA")).isEqualTo(1L);

        verify(transacaoRepository).countByNumeroContaAndStatus("12345-6", "APROVADA");
        verify(transacaoRepository).countByNumeroContaAndStatus("99999-9", "REPROVADA");
    }

    @Test
    @DisplayName("Deve salvar transação")
    void deveSalvarTransacao() {
        // Given
        when(transacaoRepository.save(transacao1)).thenReturn(transacao1);

        // When
        Transacao resultado = transacaoRepository.save(transacao1);

        // Then
        assertThat(resultado).isNotNull();
        assertThat(resultado.getIdTransacao()).isEqualTo("tx-001");
        assertThat(resultado.getNumeroConta()).isEqualTo("12345-6");
        verify(transacaoRepository).save(transacao1);
    }

    @Test
    @DisplayName("Deve deletar transação por id")
    void deveDeletarTransacaoPorId() {
        // Given
        doNothing().when(transacaoRepository).deleteById("id-1");

        // When
        transacaoRepository.deleteById("id-1");

        // Then
        verify(transacaoRepository).deleteById("id-1");
    }

    @Test
    @DisplayName("Deve contar total de transações")
    void deveContarTotalDeTransacoes() {
        // Given
        when(transacaoRepository.count()).thenReturn(3L);

        // When
        long total = transacaoRepository.count();

        // Then
        assertThat(total).isEqualTo(3L);
        verify(transacaoRepository).count();
    }

    private Transacao criarTransacao(
            String id,
            String idTransacao,
            String numeroConta,
            BigDecimal valor,
            String tipo,
            String status,
            String descricao,
            LocalDateTime dataHora,
            BigDecimal saldoAposTransacao
    ) {
        Transacao transacao = new Transacao();
        transacao.setId(id);
        transacao.setIdTransacao(idTransacao);
        transacao.setNumeroConta(numeroConta);
        transacao.setValor(valor);
        transacao.setTipo(tipo);
        transacao.setStatus(status);
        transacao.setDescricao(descricao);
        transacao.setDataHora(dataHora);
        transacao.setSaldoAposTransacao(saldoAposTransacao);
        return transacao;
    }
}