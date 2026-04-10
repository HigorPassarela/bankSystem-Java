package br.com.banksystem.extratos.controller;

import br.com.banksystem.extratos.dto.RespostaDTO;
import br.com.banksystem.extratos.dto.TransacaoDTO;
import br.com.banksystem.extratos.service.ExtratoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Controller REST para consulta de extratos bancários e geração de PDFs.
 */
@RestController
@RequestMapping("/api/extratos")
@Tag(name = "Extratos", description = "Consulta de extrato e download de PDF")
public class ExtratoController {

    private final ExtratoService extratoService;

    public ExtratoController(ExtratoService extratoService) {
        this.extratoService = extratoService;
    }

    @GetMapping("/conta/{numeroConta}")
    @Operation(
            summary = "Listar todas as transações de uma conta",
            description = "Retorna o histórico completo de transações ordenado por data decrescente.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<RespostaDTO<List<TransacaoDTO>>> listarPorConta(
            @PathVariable String numeroConta) {
        List<TransacaoDTO> transacoes = extratoService.listarPorConta(numeroConta);
        return ResponseEntity.ok(RespostaDTO.sucesso(transacoes,
                transacoes.size() + " transação(ões) encontrada(s)"));
    }

    @GetMapping("/conta/{numeroConta}/paginado")
    @Operation(
            summary = "Listar transações paginadas",
            description = "Útil para listas longas. Padrão: página 0, 20 itens por página.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<RespostaDTO<Page<TransacaoDTO>>> listarPaginado(
            @PathVariable String numeroConta,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "20") int tamanho) {
        Page<TransacaoDTO> pagina_resultado =
                extratoService.listarPorContaPaginado(numeroConta, pagina, tamanho);
        return ResponseEntity.ok(RespostaDTO.sucesso(pagina_resultado,
                "Página " + (pagina + 1) + " de " + pagina_resultado.getTotalPages()));
    }

    @GetMapping("/periodo")
    @Operation(
            summary = "Extrato por período",
            description = "Filtra as transações entre duas datas. Formato ISO: 2024-01-01T00:00:00",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<RespostaDTO<List<TransacaoDTO>>> listarPorPeriodo(
            @RequestParam String numeroConta,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fim) {
        List<TransacaoDTO> transacoes = extratoService.listarPorPeriodo(numeroConta, inicio, fim);
        return ResponseEntity.ok(RespostaDTO.sucesso(transacoes,
                transacoes.size() + " transação(ões) no período"));
    }

    @GetMapping("/tipo")
    @Operation(
            summary = "Filtrar por tipo de transação",
            description = "Tipos: DEPOSITO | DEBITO | CREDITO | TRANSFERENCIA_SAIDA | TRANSFERENCIA_ENTRADA",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<RespostaDTO<List<TransacaoDTO>>> listarPorTipo(
            @RequestParam String numeroConta,
            @RequestParam String tipo) {
        List<TransacaoDTO> transacoes = extratoService.listarPorTipo(numeroConta, tipo);
        return ResponseEntity.ok(RespostaDTO.sucesso(transacoes,
                transacoes.size() + " transação(ões) do tipo " + tipo));
    }

    @GetMapping("/pdf/{numeroConta}")
    @Operation(
            summary = "Download PDF — extrato completo",
            description = "Gera e faz download do PDF com histórico completo da conta.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<byte[]> downloadPdfCompleto(@PathVariable String numeroConta) {
        byte[] pdf = extratoService.gerarPdf(numeroConta);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=extrato-" + numeroConta + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    @GetMapping("/pdf/{numeroConta}/periodo")
    @Operation(
            summary = "Download PDF — extrato por período",
            description = "Gera PDF filtrando as transações entre as datas informadas.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<byte[]> downloadPdfPorPeriodo(
            @PathVariable String numeroConta,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fim) {
        byte[] pdf = extratoService.gerarPdfPorPeriodo(numeroConta, inicio, fim);
        String nomeArquivo = "extrato-" + numeroConta + "-"
                + inicio.toLocalDate() + "-a-" + fim.toLocalDate() + ".pdf";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + nomeArquivo)
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }
}
