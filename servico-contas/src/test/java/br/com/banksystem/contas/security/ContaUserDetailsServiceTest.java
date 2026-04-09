package br.com.banksystem.contas.security;

import br.com.banksystem.contas.model.Conta;
import br.com.banksystem.contas.repository.ContaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes para ContaUserDetailsService")
class ContaUserDetailsServiceTest {

    @Mock
    private ContaRepository contaRepository;

    @InjectMocks
    private ContaUserDetailsService contaUserDetailsService;

    private Conta contaAtiva;
    private Conta contaInativa;
    private final String numeroConta = "12345678";
    private final String senhaHash = "$2a$10$hashedPassword";
    private final String role = "ROLE_USER";

    @BeforeEach
    void setUp() {
        contaAtiva = new Conta();
        contaAtiva.setNumeroConta(numeroConta);
        contaAtiva.setSenhaHash(senhaHash);
        contaAtiva.setRole(role);
        contaAtiva.setAtiva(true);

        contaInativa = new Conta();
        contaInativa.setNumeroConta(numeroConta);
        contaInativa.setSenhaHash(senhaHash);
        contaInativa.setRole(role);
        contaInativa.setAtiva(false);
    }

    @Test
    @DisplayName("Deve carregar usuário com sucesso quando conta existe e está ativa")
    void deveCarregarUsuarioComSucessoQuandoContaExisteEEstaAtiva() {
        // Given
        when(contaRepository.findByNumeroConta(numeroConta))
                .thenReturn(Optional.of(contaAtiva));

        // When
        UserDetails userDetails = contaUserDetailsService.loadUserByUsername(numeroConta);

        // Then
        assertThat(userDetails).isNotNull();
        assertThat(userDetails.getUsername()).isEqualTo(numeroConta);
        assertThat(userDetails.getPassword()).isEqualTo(senhaHash);

        // Verificação das authorities usando stream
        assertThat(userDetails.getAuthorities()).hasSize(1);
        assertThat(userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .findFirst())
                .isPresent()
                .contains(role);

        verify(contaRepository).findByNumeroConta(numeroConta);
    }

    @Test
    @DisplayName("Deve lançar UsernameNotFoundException quando conta não existe")
    void deveLancarUsernameNotFoundExceptionQuandoContaNaoExiste() {
        // Given
        String numeroContaInexistente = "99999999";
        when(contaRepository.findByNumeroConta(numeroContaInexistente))
                .thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> contaUserDetailsService.loadUserByUsername(numeroContaInexistente))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessage("Conta não encontrada: " + numeroContaInexistente);

        verify(contaRepository).findByNumeroConta(numeroContaInexistente);
    }

    @Test
    @DisplayName("Deve lançar UsernameNotFoundException quando conta está inativa")
    void deveLancarUsernameNotFoundExceptionQuandoContaEstaInativa() {
        // Given
        when(contaRepository.findByNumeroConta(numeroConta))
                .thenReturn(Optional.of(contaInativa));

        // When & Then
        assertThatThrownBy(() -> contaUserDetailsService.loadUserByUsername(numeroConta))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessage("Conta inativa: " + numeroConta);

        verify(contaRepository).findByNumeroConta(numeroConta);
    }

    @Test
    @DisplayName("Deve carregar usuário com role diferente")
    void deveCarregarUsuarioComRoleDiferente() {
        // Given
        String roleAdmin = "ROLE_ADMIN";
        contaAtiva.setRole(roleAdmin);

        when(contaRepository.findByNumeroConta(numeroConta))
                .thenReturn(Optional.of(contaAtiva));

        // When
        UserDetails userDetails = contaUserDetailsService.loadUserByUsername(numeroConta);

        // Then
        assertThat(userDetails.getAuthorities()).hasSize(1);
        assertThat(userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .findFirst())
                .isPresent()
                .contains(roleAdmin);

        verify(contaRepository).findByNumeroConta(numeroConta);
    }

    @Test
    @DisplayName("Deve tratar número da conta nulo")
    void deveTratarNumeroContaNulo() {
        // Given
        when(contaRepository.findByNumeroConta(null))
                .thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> contaUserDetailsService.loadUserByUsername(null))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessage("Conta não encontrada: null");

        verify(contaRepository).findByNumeroConta(null);
    }

    @Test
    @DisplayName("Deve tratar número da conta vazio")
    void deveTratarNumeroContaVazio() {
        // Given
        String numeroContaVazio = "";
        when(contaRepository.findByNumeroConta(numeroContaVazio))
                .thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> contaUserDetailsService.loadUserByUsername(numeroContaVazio))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessage("Conta não encontrada: ");

        verify(contaRepository).findByNumeroConta(numeroContaVazio);
    }

    @Test
    @DisplayName("Deve verificar que UserDetails está habilitado por padrão")
    void deveVerificarQueUserDetailsEstaHabilitadoPorPadrao() {
        // Given
        when(contaRepository.findByNumeroConta(numeroConta))
                .thenReturn(Optional.of(contaAtiva));

        // When
        UserDetails userDetails = contaUserDetailsService.loadUserByUsername(numeroConta);

        // Then
        assertThat(userDetails.isEnabled()).isTrue();
        assertThat(userDetails.isAccountNonExpired()).isTrue();
        assertThat(userDetails.isAccountNonLocked()).isTrue();
        assertThat(userDetails.isCredentialsNonExpired()).isTrue();
    }
}