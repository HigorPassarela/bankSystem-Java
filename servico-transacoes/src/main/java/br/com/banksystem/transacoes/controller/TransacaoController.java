package br.com.banksystem.transacoes.controller;

import br.com.banksystem.transacoes.dto.*;
import br.com.banksystem.transacoes.service.TransacaoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * Controller REST para processamento de transações bancárias.
 */
@RestController
@RequestMapping("/api/transacoes")
@Tag(name = "Transações", description = "Depósito, débito, crédito, transferências e consulta de saldo")
public class TransacaoController {

    private final TransacaoService transacaoService;

    public TransacaoController(TransacaoService transacaoService) {
        this.transacaoService = transacaoService;
    }

    @PostMapping("/deposito")
    @Operation(
            summary = "Depositar valor em conta",
            description = "Credita o valor diretamente no saldo disponível. Não exige PIN de transferência.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<RespostaDTO<TransacaoRespostaDTO>> deposito(
            @AuthenticationPrincipal String numeroConta,
            @Valid @RequestBody DepositoDTO dto) {
        TransacaoRespostaDTO resultado = transacaoService.processarDeposito(numeroConta, dto);
        return ResponseEntity.ok(RespostaDTO.sucesso(resultado,
                "Depósito de R$ " + dto.valor() + " realizado com sucesso"));
    }

    @PostMapping("/debito")
    @Operation(
            summary = "Débito no saldo da conta",
            description = "Debita valor do saldo disponível. Falha se saldo insuficiente.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<RespostaDTO<TransacaoRespostaDTO>> debito(
            @AuthenticationPrincipal String numeroConta,
            @Valid @RequestBody DebitoDTO dto) {
        TransacaoRespostaDTO resultado = transacaoService.processarDebito(numeroConta, dto);
        return ResponseEntity.ok(RespostaDTO.sucesso(resultado, "Débito processado com sucesso"));
    }

    @PostMapping("/credito")
    @Operation(
            summary = "Crédito usando limite da conta",
            description = "Usa o limite disponível (modalidade 'pagar depois').",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<RespostaDTO<TransacaoRespostaDTO>> credito(
            @AuthenticationPrincipal String numeroConta,
            @Valid @RequestBody CreditoDTO dto) {
        TransacaoRespostaDTO resultado = transacaoService.processarCredito(numeroConta, dto);
        return ResponseEntity.ok(RespostaDTO.sucesso(resultado, "Crédito processado com sucesso"));
    }

    @PostMapping("/transferencia")
    @Operation(
            summary = "Transferência entre contas",
            description = "Transfere saldo para outra conta. Requer PIN de 4 dígitos cadastrado na criação da conta.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<RespostaDTO<TransferenciaRespostaDTO>> transferencia(
            @AuthenticationPrincipal String numeroConta,
            @Valid @RequestBody TransferenciaDTO dto,
            HttpServletRequest request) {
        TransferenciaRespostaDTO resultado =
                transacaoService.processarTransferencia(numeroConta, dto, extrairToken(request));
        return ResponseEntity.ok(RespostaDTO.sucesso(resultado, "Transferência realizada com sucesso"));
    }

    @GetMapping("/saldo")
    @Operation(
            summary = "Consultar saldo e limite disponíveis",
            description = "Retorna saldo disponível e limite de crédito da conta.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<RespostaDTO<SaldoDTO>> consultarSaldo(
            @AuthenticationPrincipal String numeroConta) {
        SaldoDTO saldo = transacaoService.consultarSaldo(numeroConta);
        return ResponseEntity.ok(RespostaDTO.sucesso(saldo, "Saldo consultado com sucesso"));
    }

    @GetMapping("/limite")
    @Operation(
            summary = "Consultar limite de crédito disponível",
            description = "Retorna o limite de crédito disponível (mesmo que /saldo).",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<RespostaDTO<SaldoDTO>> consultarLimite(
            @AuthenticationPrincipal String numeroConta) {
        SaldoDTO limite = transacaoService.consultarSaldo(numeroConta);
        return ResponseEntity.ok(RespostaDTO.sucesso(limite, "Limite consultado com sucesso"));
    }

    private String extrairToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        return (header != null && header.startsWith("Bearer ")) ? header.substring(7) : "";
    }
}