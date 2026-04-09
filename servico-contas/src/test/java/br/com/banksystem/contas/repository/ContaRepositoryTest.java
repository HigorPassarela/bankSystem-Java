package br.com.banksystem.contas.repository;

import br.com.banksystem.contas.model.Conta;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes do ContaRepository")
class ContaRepositoryTest {

    @Mock
    private ContaRepository contaRepository;

    private Conta contaTeste;

    @BeforeEach
    void setUp() {
        contaTeste = criarConta(
                "12345-6",
                "12345678901",
                "teste@email.com",
                "João Silva",
                "token123"
        );
    }

    @Test
    @DisplayName("Deve encontrar conta por número da conta")
    void deveFindByNumeroConta() {
        // Given
        when(contaRepository.findByNumeroConta("12345-6"))
                .thenReturn(Optional.of(contaTeste));

        // When
        Optional<Conta> resultado = contaRepository.findByNumeroConta("12345-6");

        // Then
        assertThat(resultado).isPresent();
        assertThat(resultado.get().getNumeroConta()).isEqualTo("12345-6");
        assertThat(resultado.get().getCpf()).isEqualTo("12345678901");
        verify(contaRepository).findByNumeroConta("12345-6");
    }

    @Test
    @DisplayName("Deve retornar vazio quando não encontrar conta por número")
    void deveRetornarVazioQuandoNaoEncontrarPorNumeroConta() {
        // Given
        when(contaRepository.findByNumeroConta("99999-9"))
                .thenReturn(Optional.empty());

        // When
        Optional<Conta> resultado = contaRepository.findByNumeroConta("99999-9");

        // Then
        assertThat(resultado).isEmpty();
        verify(contaRepository).findByNumeroConta("99999-9");
    }

    @Test
    @DisplayName("Deve encontrar conta por CPF")
    void deveFindByCpf() {
        // Given
        when(contaRepository.findByCpf("12345678901"))
                .thenReturn(Optional.of(contaTeste));

        // When
        Optional<Conta> resultado = contaRepository.findByCpf("12345678901");

        // Then
        assertThat(resultado).isPresent();
        assertThat(resultado.get().getCpf()).isEqualTo("12345678901");
        verify(contaRepository).findByCpf("12345678901");
    }

    @Test
    @DisplayName("Deve encontrar conta por email")
    void deveFindByEmail() {
        // Given
        when(contaRepository.findByEmail("teste@email.com"))
                .thenReturn(Optional.of(contaTeste));

        // When
        Optional<Conta> resultado = contaRepository.findByEmail("teste@email.com");

        // Then
        assertThat(resultado).isPresent();
        assertThat(resultado.get().getEmail()).isEqualTo("teste@email.com");
        verify(contaRepository).findByEmail("teste@email.com");
    }

    @Test
    @DisplayName("Deve encontrar conta por token de verificação")
    void deveFindByTokenVerificacaoEmail() {
        // Given
        when(contaRepository.findByTokenVerificacaoEmail("token123"))
                .thenReturn(Optional.of(contaTeste));

        // When
        Optional<Conta> resultado = contaRepository.findByTokenVerificacaoEmail("token123");

        // Then
        assertThat(resultado).isPresent();
        assertThat(resultado.get().getTokenVerificacaoEmail()).isEqualTo("token123");
        verify(contaRepository).findByTokenVerificacaoEmail("token123");
    }

    @Test
    @DisplayName("Deve verificar existência por número da conta")
    void deveExistsByNumeroConta() {
        // Given
        when(contaRepository.existsByNumeroConta("12345-6")).thenReturn(true);
        when(contaRepository.existsByNumeroConta("99999-9")).thenReturn(false);

        // When & Then
        assertThat(contaRepository.existsByNumeroConta("12345-6")).isTrue();
        assertThat(contaRepository.existsByNumeroConta("99999-9")).isFalse();

        verify(contaRepository).existsByNumeroConta("12345-6");
        verify(contaRepository).existsByNumeroConta("99999-9");
    }

    @Test
    @DisplayName("Deve verificar existência por CPF")
    void deveExistsByCpf() {
        // Given
        when(contaRepository.existsByCpf("12345678901")).thenReturn(true);
        when(contaRepository.existsByCpf("00000000000")).thenReturn(false);

        // When & Then
        assertThat(contaRepository.existsByCpf("12345678901")).isTrue();
        assertThat(contaRepository.existsByCpf("00000000000")).isFalse();

        verify(contaRepository).existsByCpf("12345678901");
        verify(contaRepository).existsByCpf("00000000000");
    }

    @Test
    @DisplayName("Deve verificar existência por email")
    void deveExistsByEmail() {
        // Given
        when(contaRepository.existsByEmail("teste@email.com")).thenReturn(true);
        when(contaRepository.existsByEmail("inexistente@email.com")).thenReturn(false);

        // When & Then
        assertThat(contaRepository.existsByEmail("teste@email.com")).isTrue();
        assertThat(contaRepository.existsByEmail("inexistente@email.com")).isFalse();

        verify(contaRepository).existsByEmail("teste@email.com");
        verify(contaRepository).existsByEmail("inexistente@email.com");
    }

    @Test
    @DisplayName("Deve salvar conta")
    void deveSalvarConta() {
        // Given
        Conta contaSalva = criarConta("12345-6", "12345678901", "teste@email.com", "João Silva", "token123");
        contaSalva.setId("507f1f77bcf86cd799439011");

        when(contaRepository.save(any(Conta.class))).thenReturn(contaSalva);

        // When
        Conta resultado = contaRepository.save(contaTeste);

        // Then
        assertThat(resultado.getId()).isNotNull();
        assertThat(resultado.getNumeroConta()).isEqualTo("12345-6");
        verify(contaRepository).save(contaTeste);
    }

    @Test
    @DisplayName("Deve deletar conta por ID")
    void deveDeletarContaPorId() {
        // Given
        String id = "507f1f77bcf86cd799439011";
        doNothing().when(contaRepository).deleteById(id);

        // When
        contaRepository.deleteById(id);

        // Then
        verify(contaRepository).deleteById(id);
    }

    @Test
    @DisplayName("Deve contar total de contas")
    void deveContarTotalDeContas() {
        // Given
        when(contaRepository.count()).thenReturn(5L);

        // When
        long total = contaRepository.count();

        // Then
        assertThat(total).isEqualTo(5L);
        verify(contaRepository).count();
    }

    private Conta criarConta(String numeroConta, String cpf, String email,
                             String nomeCompleto, String token) {
        Conta conta = new Conta();
        conta.setNumeroConta(numeroConta);
        conta.setCpf(cpf);
        conta.setEmail(email);
        conta.setNomeCompleto(nomeCompleto);
        conta.setTokenVerificacaoEmail(token);
        return conta;
    }
}