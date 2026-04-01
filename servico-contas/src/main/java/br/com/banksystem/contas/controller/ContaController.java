package br.com.banksystem.contas.controller;

import br.com.banksystem.contas.dto.*;
import br.com.banksystem.contas.service.ContaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

/**
 * Controller REST para gerenciamento de contas bancárias.
 */
@RestController
@RequestMapping("/api/contas")
@Tag(name = "Contas", description = "Endpoints de gerenciamento e autenticação de contas bancárias")
public class ContaController {

    private final ContaService contaService;

    public ContaController(ContaService contaService) {
        this.contaService = contaService;
    }

    @PostMapping("/criar")
    @Operation(summary = "Criar nova conta bancária (envia e-mail de verificação via MailHog)")
    public ResponseEntity<RespostaDTO<PerfilContaDTO>> criarConta(@Valid @RequestBody CriarContaDTO dto) {
        PerfilContaDTO perfil = contaService.criarConta(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(RespostaDTO.sucesso(perfil, "Conta criada! Verifique seu e-mail para ativar a conta."));
    }

    @GetMapping("/verificar-email")
    @Operation(summary = "Verificar e-mail via token recebido no link (MailHog)")
    public ResponseEntity<RespostaDTO<VerificarEmailDTO>> verificarEmail(@RequestParam String token) {
        VerificarEmailDTO resultado = contaService.verificarEmail(token);
        return ResponseEntity.ok(RespostaDTO.sucesso(resultado, resultado.mensagem()));
    }

    @PostMapping("/reenviar-verificacao")
    @Operation(summary = "Reenviar e-mail de verificação")
    public ResponseEntity<RespostaDTO<Void>> reenviarVerificacao(@RequestParam String email) {
        contaService.reenviarVerificacao(email);
        return ResponseEntity.ok(RespostaDTO.sucesso(null, "E-mail de verificação reenviado com sucesso"));
    }

    @PostMapping("/login")
    @Operation(summary = "Autenticar conta e obter token JWT (requer e-mail verificado)")
    public ResponseEntity<RespostaDTO<TokenDTO>> login(@Valid @RequestBody LoginDTO dto) {
        TokenDTO token = contaService.autenticar(dto);
        return ResponseEntity.ok(RespostaDTO.sucesso(token, "Login realizado com sucesso"));
    }

    @GetMapping("/perfil")
    @Operation(summary = "Obter perfil da conta autenticada", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<RespostaDTO<PerfilContaDTO>> obterPerfil(
            @AuthenticationPrincipal UserDetails userDetails) {
        PerfilContaDTO perfil = contaService.obterPerfil(userDetails.getUsername());
        return ResponseEntity.ok(RespostaDTO.sucesso(perfil, "Perfil obtido com sucesso"));
    }

    @PutMapping("/atualizar")
    @Operation(summary = "Atualizar dados da conta", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<RespostaDTO<PerfilContaDTO>> atualizarConta(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody AtualizarContaDTO dto) {
        PerfilContaDTO perfil = contaService.atualizarConta(userDetails.getUsername(), dto);
        return ResponseEntity.ok(RespostaDTO.sucesso(perfil, "Conta atualizada com sucesso"));
    }

    @PutMapping("/senha-transferencia")
    @Operation(summary = "Atualizar PIN de 4 dígitos para transferências", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<RespostaDTO<Void>> atualizarSenhaTransferencia(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody AtualizarSenhaTransferenciaDTO dto) {
        contaService.atualizarSenhaTransferencia(userDetails.getUsername(), dto);
        return ResponseEntity.ok(RespostaDTO.sucesso(null, "Senha de transferência atualizada com sucesso"));
    }

    @GetMapping("/buscar/{numeroConta}")
    @Operation(summary = "Buscar dados de conta por número (para transferências)", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<RespostaDTO<PerfilContaDTO>> buscarConta(@PathVariable String numeroConta) {
        PerfilContaDTO perfil = contaService.buscarContaPorNumero(numeroConta);
        return ResponseEntity.ok(RespostaDTO.sucesso(perfil, "Conta encontrada"));
    }

    @PostMapping("/validar-senha-transferencia")
    @Operation(summary = "Validar PIN de transferência (uso interno entre serviços)", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<RespostaDTO<Boolean>> validarSenhaTransferencia(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam String senhaTransferencia) {
        boolean valido = contaService.validarSenhaTransferencia(userDetails.getUsername(), senhaTransferencia);
        return ResponseEntity.ok(RespostaDTO.sucesso(valido, valido ? "Senha válida" : "Senha inválida"));
    }
}
