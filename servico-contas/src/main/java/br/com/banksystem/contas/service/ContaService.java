package br.com.banksystem.contas.service;

import br.com.banksystem.contas.dto.*;
import br.com.banksystem.contas.exception.ContaJaExisteException;
import br.com.banksystem.contas.exception.ContaNaoEncontradaException;
import br.com.banksystem.contas.mapper.ContaMapper;
import br.com.banksystem.contas.model.Conta;
import br.com.banksystem.contas.model.enums.StatusConta;
import br.com.banksystem.contas.repository.ContaRepository;
import br.com.banksystem.contas.security.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Random;
import java.util.UUID;

/**
 * Serviço de negócio para gestão de contas bancárias.
 * <p>
 * Fluxo de status:
 * criarConta()    → PENDENTE_EMAIL
 * verificarEmail() → ATIVA
 */
@Service
public class ContaService {

    private static final Logger log = LoggerFactory.getLogger(ContaService.class);
    private static final long SALDO_INICIAL_CENTAVOS = 0L;       // sem saldo inicial
    private static final long LIMITE_INICIAL_CENTAVOS = 500000L;  // R$ 5.000,00

    private final ContaRepository contaRepository;
    private final ContaMapper contaMapper;
    private final PasswordEncoder codificadorSenha;
    private final AuthenticationManager gerenciadorAutenticacao;
    private final JwtUtil jwtUtil;
    private final RedisTemplate<String, String> redisTemplate;
    private final EmailService emailService;

    public ContaService(ContaRepository contaRepository, ContaMapper contaMapper,
                        PasswordEncoder codificadorSenha,
                        AuthenticationManager gerenciadorAutenticacao,
                        JwtUtil jwtUtil,
                        RedisTemplate<String, String> redisTemplate,
                        EmailService emailService) {
        this.contaRepository = contaRepository;
        this.contaMapper = contaMapper;
        this.codificadorSenha = codificadorSenha;
        this.gerenciadorAutenticacao = gerenciadorAutenticacao;
        this.jwtUtil = jwtUtil;
        this.redisTemplate = redisTemplate;
        this.emailService = emailService;
    }

    /**
     * Cria uma nova conta bancária.
     * Status inicial: PENDENTE_EMAIL.
     * A conta só fica ATIVA após a confirmação do e-mail.
     */
    public PerfilContaDTO criarConta(CriarContaDTO dto) {
        log.info("Criando conta para CPF: {}", dto.cpf());

        if (contaRepository.existsByCpf(dto.cpf()))
            throw new ContaJaExisteException("CPF já cadastrado no sistema");
        if (contaRepository.existsByEmail(dto.email()))
            throw new ContaJaExisteException("E-mail já cadastrado no sistema");

        String numeroConta = gerarNumeroConta();
        String senhaHash = codificadorSenha.encode(dto.senha());
        String senhaTransferenciaHash = codificadorSenha.encode(dto.senhaTransferencia());
        String tokenVerificacao = UUID.randomUUID().toString();

        Conta conta = contaMapper.paraEntidade(dto, senhaHash, senhaTransferenciaHash, numeroConta);
        conta.setTokenVerificacaoEmail(tokenVerificacao);
        conta.setTokenVerificacaoExpiracao(LocalDateTime.now().plusHours(24));
        conta = contaRepository.save(conta);

        // Inicializa saldo = 0 e limite no Redis (bloqueado até verificação)
        redisTemplate.opsForValue().set("saldo:" + numeroConta,
                String.valueOf(SALDO_INICIAL_CENTAVOS));
        redisTemplate.opsForValue().set("limite:" + numeroConta,
                String.valueOf(LIMITE_INICIAL_CENTAVOS));

        emailService.enviarVerificacaoEmail(
                dto.email(), dto.nomeCompleto(), numeroConta, tokenVerificacao);

        log.info("Conta criada com status PENDENTE_EMAIL: {}", numeroConta);
        return contaMapper.paraPerfilDTO(conta);
    }

    /**
     * Verifica e-mail via token do link enviado pelo MailHog.
     * Transição de status: PENDENTE_EMAIL → ATIVA.
     */
    public VerificarEmailDTO verificarEmail(String token) {
        Conta conta = contaRepository.findByTokenVerificacaoEmail(token)
                .orElseThrow(() -> new ContaNaoEncontradaException(
                        "Token de verificação inválido ou já utilizado"));

        if (conta.getStatus() == StatusConta.ATIVA) {
            return new VerificarEmailDTO("E-mail já verificado anteriormente. Conta está ATIVA.", true);
        }
        if (conta.getTokenVerificacaoExpiracao().isBefore(LocalDateTime.now())) {
            throw new ContaNaoEncontradaException(
                    "Token expirado. Solicite um novo e-mail de verificação.");
        }

        conta.setStatus(StatusConta.ATIVA);          // PENDENTE_EMAIL → ATIVA
        conta.setEmailVerificado(true);
        conta.setTokenVerificacaoEmail(null);
        conta.setTokenVerificacaoExpiracao(null);
        conta.setDataAtualizacao(LocalDateTime.now());
        contaRepository.save(conta);

        log.info("Conta ativada após verificação de e-mail: {}", conta.getNumeroConta());
        return new VerificarEmailDTO(
                "E-mail verificado com sucesso! Conta " + conta.getNumeroConta() + " está ATIVA.", true);
    }

    /**
     * Reenvia e-mail de verificação para contas ainda PENDENTE_EMAIL.
     */
    public void reenviarVerificacao(String email) {
        Conta conta = contaRepository.findByEmail(email)
                .orElseThrow(() -> new ContaNaoEncontradaException(
                        "Conta não encontrada para o e-mail informado"));

        if (conta.getStatus() == StatusConta.ATIVA) {
            throw new ContaJaExisteException("E-mail já verificado — conta está ATIVA");
        }

        String novoToken = UUID.randomUUID().toString();
        conta.setTokenVerificacaoEmail(novoToken);
        conta.setTokenVerificacaoExpiracao(LocalDateTime.now().plusHours(24));
        contaRepository.save(conta);

        emailService.enviarVerificacaoEmail(
                email, conta.getNomeCompleto(), conta.getNumeroConta(), novoToken);
        log.info("E-mail de verificação reenviado para: {}", email);
    }

    /**
     * Autentica a conta — exige status ATIVA.
     * Contas PENDENTE_EMAIL são bloqueadas com mensagem clara.
     */
    public TokenDTO autenticar(LoginDTO dto) {
        log.info("Tentativa de login para conta: {}", dto.numeroConta());

        Conta conta = contaRepository.findByNumeroConta(dto.numeroConta())
                .orElseThrow(() -> new BadCredentialsException("Número da conta ou senha inválidos"));

        if (conta.getStatus() == StatusConta.PENDENTE_EMAIL) {
            throw new BadCredentialsException(
                    "Conta pendente de verificação. Acesse seu e-mail e confirme sua conta.");
        }
        if (conta.getStatus() == StatusConta.SUSPENSA) {
            throw new BadCredentialsException("Conta suspensa. Entre em contato com o suporte.");
        }
        if (conta.getStatus() == StatusConta.ENCERRADA) {
            throw new BadCredentialsException("Conta encerrada.");
        }

        gerenciadorAutenticacao.authenticate(
                new UsernamePasswordAuthenticationToken(dto.numeroConta(), dto.senha()));

        String token = jwtUtil.gerarToken(
                conta.getNumeroConta(), conta.getNomeCompleto(), conta.getRole());

        log.info("Login realizado com sucesso: {}", dto.numeroConta());
        return new TokenDTO(token, "Bearer", conta.getNumeroConta(),
                conta.getNomeCompleto(), jwtUtil.obterExpiracaoMs());
    }

    public PerfilContaDTO obterPerfil(String numeroConta) {
        return contaMapper.paraPerfilDTO(buscarOuLancar(numeroConta));
    }

    public PerfilContaDTO atualizarConta(String numeroConta, AtualizarContaDTO dto) {
        Conta conta = buscarOuLancar(numeroConta);
        if (dto.nomeCompleto() != null && !dto.nomeCompleto().isBlank())
            conta.setNomeCompleto(dto.nomeCompleto());
        if (dto.email() != null && !dto.email().isBlank()) {
            if (!dto.email().equals(conta.getEmail()) && contaRepository.existsByEmail(dto.email()))
                throw new ContaJaExisteException("E-mail já utilizado por outra conta");
            conta.setEmail(dto.email());
        }
        if (dto.telefone() != null && !dto.telefone().isBlank())
            conta.setTelefone(dto.telefone());
        if (dto.novaSenha() != null && !dto.novaSenha().isBlank())
            conta.setSenhaHash(codificadorSenha.encode(dto.novaSenha()));
        conta.setDataAtualizacao(LocalDateTime.now());
        return contaMapper.paraPerfilDTO(contaRepository.save(conta));
    }

    public void atualizarSenhaTransferencia(String numeroConta, AtualizarSenhaTransferenciaDTO dto) {
        Conta conta = buscarOuLancar(numeroConta);
        if (!codificadorSenha.matches(dto.senhaAtual(), conta.getSenhaTransferenciaHash()))
            throw new BadCredentialsException("Senha de transferência atual incorreta");
        conta.setSenhaTransferenciaHash(codificadorSenha.encode(dto.novaSenha()));
        conta.setDataAtualizacao(LocalDateTime.now());
        contaRepository.save(conta);
        log.info("PIN de transferência atualizado: {}", numeroConta);
    }

    public boolean validarSenhaTransferencia(String numeroConta, String senhaTransferencia) {
        Conta conta = buscarOuLancar(numeroConta);
        return codificadorSenha.matches(senhaTransferencia, conta.getSenhaTransferenciaHash());
    }

    public PerfilContaDTO buscarContaPorNumero(String numeroConta) {
        Conta conta = buscarOuLancar(numeroConta);
        if (conta.getStatus() != StatusConta.ATIVA)
            throw new ContaNaoEncontradaException(
                    "Conta de destino não está ativa (status: " + conta.getStatus() + ")");
        return contaMapper.paraPerfilDTO(conta);
    }

    private Conta buscarOuLancar(String numeroConta) {
        return contaRepository.findByNumeroConta(numeroConta)
                .orElseThrow(() -> new ContaNaoEncontradaException(
                        "Conta não encontrada: " + numeroConta));
    }

    private String gerarNumeroConta() {
        Random random = new Random();
        String numero;
        do {
            numero = String.format("%08d", random.nextInt(100000000));
        }
        while (contaRepository.existsByNumeroConta(numero));
        return numero;
    }
}
