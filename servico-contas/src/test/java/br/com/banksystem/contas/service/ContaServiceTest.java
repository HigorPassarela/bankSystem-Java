package br.com.banksystem.contas.service;

import br.com.banksystem.contas.dto.*;
import br.com.banksystem.contas.exception.ContaJaExisteException;
import br.com.banksystem.contas.exception.ContaNaoEncontradaException;
import br.com.banksystem.contas.mapper.ContaMapper;
import br.com.banksystem.contas.model.Conta;
import br.com.banksystem.contas.model.enums.StatusConta;
import br.com.banksystem.contas.repository.ContaRepository;
import br.com.banksystem.contas.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class ContaServiceTest {

    @Mock
    private ContaRepository contaRepository;

    @Mock
    private ContaMapper contaMapper;

    @Mock
    private PasswordEncoder codificadorSenha;

    @Mock
    private AuthenticationManager gerenciadorAutenticacao;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private ContaService contaService;

    private CriarContaDTO criarContaDTO;
    private Conta conta;
    private PerfilContaDTO perfilContaDTO;

    @BeforeEach
    void setUp() {
        criarContaDTO = new CriarContaDTO(
                "João Silva",
                "12345678901",
                "joao@email.com",
                "11999999999",
                "senha123",
                "1234"
        );

        conta = new Conta();
        conta.setNumeroConta("12345678");
        conta.setNomeCompleto("João Silva");
        conta.setCpf("12345678901");
        conta.setEmail("joao@email.com");
        conta.setTelefone("11999999999");
        conta.setSenhaHash("senhaHash");
        conta.setSenhaTransferenciaHash("pinHash");
        conta.setRole("ROLE_USUARIO");
        conta.setStatus(StatusConta.PENDENTE_EMAIL);
        conta.setEmailVerificado(false);
        conta.setDataCriacao(LocalDateTime.now());
        conta.setDataAtualizacao(LocalDateTime.now());

        perfilContaDTO = new PerfilContaDTO(
                "12345678",
                "João Silva",
                "12345678901",
                "joao@email.com",
                "11999999999",
                StatusConta.PENDENTE_EMAIL,
                false,
                false,
                conta.getDataCriacao()
        );
    }

    @Test
    void deveCriarContaComSucesso() {
        when(contaRepository.existsByCpf(criarContaDTO.cpf())).thenReturn(false);
        when(contaRepository.existsByEmail(criarContaDTO.email())).thenReturn(false);
        when(codificadorSenha.encode(criarContaDTO.senha())).thenReturn("senhaHash");
        when(codificadorSenha.encode(criarContaDTO.senhaTransferencia())).thenReturn("pinHash");
        when(contaMapper.paraEntidade(any(), anyString(), anyString(), anyString())).thenReturn(conta);
        when(contaRepository.save(any(Conta.class))).thenReturn(conta);
        when(contaMapper.paraPerfilDTO(any(Conta.class))).thenReturn(perfilContaDTO);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        PerfilContaDTO resultado = contaService.criarConta(criarContaDTO);

        assertNotNull(resultado);
        assertEquals("João Silva", resultado.nomeCompleto());

        verify(contaRepository).save(any(Conta.class));
        verify(emailService).enviarVerificacaoEmail(
                eq("joao@email.com"),
                eq("João Silva"),
                anyString(),
                anyString()
        );
        verify(valueOperations, times(2)).set(anyString(), anyString());
    }

    @Test
    void deveLancarExcecaoQuandoCpfJaExiste() {
        when(contaRepository.existsByCpf(criarContaDTO.cpf())).thenReturn(true);

        ContaJaExisteException ex = assertThrows(
                ContaJaExisteException.class,
                () -> contaService.criarConta(criarContaDTO)
        );

        assertEquals("CPF já cadastrado no sistema", ex.getMessage());
        verify(contaRepository, never()).save(any());
    }

    @Test
    void deveLancarExcecaoQuandoEmailJaExiste() {
        when(contaRepository.existsByCpf(criarContaDTO.cpf())).thenReturn(false);
        when(contaRepository.existsByEmail(criarContaDTO.email())).thenReturn(true);

        ContaJaExisteException ex = assertThrows(
                ContaJaExisteException.class,
                () -> contaService.criarConta(criarContaDTO)
        );

        assertEquals("E-mail já cadastrado no sistema", ex.getMessage());
        verify(contaRepository, never()).save(any());
    }

    @Test
    void deveVerificarEmailComSucesso() {
        conta.setTokenVerificacaoEmail("token123");
        conta.setTokenVerificacaoExpiracao(LocalDateTime.now().plusHours(1));
        conta.setStatus(StatusConta.PENDENTE_EMAIL);

        when(contaRepository.findByTokenVerificacaoEmail("token123")).thenReturn(Optional.of(conta));

        VerificarEmailDTO resultado = contaService.verificarEmail("token123");

        assertTrue(resultado.verificado());
        assertTrue(resultado.mensagem().contains("E-mail verificado com sucesso"));

        verify(contaRepository).save(conta);
        assertEquals(StatusConta.ATIVA, conta.getStatus());
        assertTrue(conta.getEmailVerificado());
    }

    @Test
    void deveRetornarMensagemQuandoContaJaEstaAtivaAoVerificarEmail() {
        conta.setStatus(StatusConta.ATIVA);
        conta.setTokenVerificacaoEmail("token123");
        conta.setTokenVerificacaoExpiracao(LocalDateTime.now().plusHours(1));

        when(contaRepository.findByTokenVerificacaoEmail("token123")).thenReturn(Optional.of(conta));

        VerificarEmailDTO resultado = contaService.verificarEmail("token123");

        assertTrue(resultado.verificado());
        assertEquals("E-mail já verificado anteriormente. Conta está ATIVA.", resultado.mensagem());
        verify(contaRepository, never()).save(any());
    }

    @Test
    void deveLancarExcecaoQuandoTokenExpirado() {
        conta.setTokenVerificacaoEmail("token123");
        conta.setTokenVerificacaoExpiracao(LocalDateTime.now().minusMinutes(1));
        conta.setStatus(StatusConta.PENDENTE_EMAIL);

        when(contaRepository.findByTokenVerificacaoEmail("token123")).thenReturn(Optional.of(conta));

        ContaNaoEncontradaException ex = assertThrows(
                ContaNaoEncontradaException.class,
                () -> contaService.verificarEmail("token123")
        );

        assertEquals("Token expirado. Solicite um novo e-mail de verificação.", ex.getMessage());
    }

    @Test
    void deveReenviarVerificacaoComSucesso() {
        conta.setStatus(StatusConta.PENDENTE_EMAIL);

        when(contaRepository.findByEmail("joao@email.com")).thenReturn(Optional.of(conta));
        when(contaRepository.save(any(Conta.class))).thenReturn(conta);

        assertDoesNotThrow(() -> contaService.reenviarVerificacao("joao@email.com"));

        verify(contaRepository).save(conta);
        verify(emailService).enviarVerificacaoEmail(
                eq("joao@email.com"),
                eq(conta.getNomeCompleto()),
                eq(conta.getNumeroConta()),
                anyString()
        );
    }

    @Test
    void deveLancarExcecaoAoReenviarSeContaJaAtiva() {
        conta.setStatus(StatusConta.ATIVA);

        when(contaRepository.findByEmail("joao@email.com")).thenReturn(Optional.of(conta));

        ContaJaExisteException ex = assertThrows(
                ContaJaExisteException.class,
                () -> contaService.reenviarVerificacao("joao@email.com")
        );

        assertEquals("E-mail já verificado — conta está ATIVA", ex.getMessage());
    }

    @Test
    void deveAutenticarContaAtivaComSucesso() {
        LoginDTO dto = new LoginDTO("12345678", "senha123");

        conta.setStatus(StatusConta.ATIVA);
        conta.setRole("ROLE_USUARIO");

        when(contaRepository.findByNumeroConta("12345678")).thenReturn(Optional.of(conta));
        when(jwtUtil.gerarToken(anyString(), anyString(), anyString())).thenReturn("jwt-token");
        when(jwtUtil.obterExpiracaoMs()).thenReturn(86400000L);

        TokenDTO resultado = contaService.autenticar(dto);

        assertNotNull(resultado);
        assertEquals("jwt-token", resultado.token());
        assertEquals("12345678", resultado.numeroConta());

        verify(gerenciadorAutenticacao).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    void deveBloquearLoginQuandoContaPendenteEmail() {
        LoginDTO dto = new LoginDTO("12345678", "senha123");
        conta.setStatus(StatusConta.PENDENTE_EMAIL);

        when(contaRepository.findByNumeroConta("12345678")).thenReturn(Optional.of(conta));

        BadCredentialsException ex = assertThrows(
                BadCredentialsException.class,
                () -> contaService.autenticar(dto)
        );

        assertEquals("Conta pendente de verificação. Acesse seu e-mail e confirme sua conta.", ex.getMessage());
    }

    @Test
    void deveBloquearLoginQuandoContaSuspensa() {
        LoginDTO dto = new LoginDTO("12345678", "senha123");
        conta.setStatus(StatusConta.SUSPENSA);

        when(contaRepository.findByNumeroConta("12345678")).thenReturn(Optional.of(conta));

        BadCredentialsException ex = assertThrows(
                BadCredentialsException.class,
                () -> contaService.autenticar(dto)
        );

        assertEquals("Conta suspensa. Entre em contato com o suporte.", ex.getMessage());
    }

    @Test
    void deveBloquearLoginQuandoContaEncerrada() {
        LoginDTO dto = new LoginDTO("12345678", "senha123");
        conta.setStatus(StatusConta.ENCERRADA);

        when(contaRepository.findByNumeroConta("12345678")).thenReturn(Optional.of(conta));

        BadCredentialsException ex = assertThrows(
                BadCredentialsException.class,
                () -> contaService.autenticar(dto)
        );

        assertEquals("Conta encerrada.", ex.getMessage());
    }

    @Test
    void deveLancarErroQuandoContaNaoExisteNoLogin() {
        LoginDTO dto = new LoginDTO("12345678", "senha123");

        when(contaRepository.findByNumeroConta("12345678")).thenReturn(Optional.empty());

        BadCredentialsException ex = assertThrows(
                BadCredentialsException.class,
                () -> contaService.autenticar(dto)
        );

        assertEquals("Número da conta ou senha inválidos", ex.getMessage());
    }
}