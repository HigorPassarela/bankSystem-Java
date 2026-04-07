package br.com.banksystem.extratos.service;

import br.com.banksystem.extratos.dto.TransacaoDTO;
import br.com.banksystem.extratos.mapper.TransacaoMapper;
import br.com.banksystem.extratos.repository.TransacaoRepository;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Serviço de consulta de extratos e geração de PDF com iText 8.
 *
 * O PDF gerado inclui:
 *  - Cabeçalho com dados da conta e período
 *  - Tabela de transações com cores por tipo
 *  - Totalizadores: entradas, saídas, saldo do período
 *  - Rodapé com data de geração
 */
@Service
public class ExtratoService {

    private static final Logger log = LoggerFactory.getLogger(ExtratoService.class);
    private static final DateTimeFormatter FMT_DATETIME = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter FMT_DATE     = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // Cores do layout
    private static final DeviceRgb COR_HEADER    = new DeviceRgb(26, 86, 219);   // azul
    private static final DeviceRgb COR_SUBHEADER = new DeviceRgb(239, 246, 255); // azul claro
    private static final DeviceRgb COR_ENTRADA   = new DeviceRgb(220, 252, 231); // verde claro
    private static final DeviceRgb COR_SAIDA     = new DeviceRgb(254, 226, 226); // vermelho claro
    private static final DeviceRgb COR_LINHA_PAR = new DeviceRgb(249, 250, 251); // cinza

    private final TransacaoRepository transacaoRepository;
    private final TransacaoMapper transacaoMapper;

    public ExtratoService(TransacaoRepository transacaoRepository, TransacaoMapper transacaoMapper) {
        this.transacaoRepository = transacaoRepository;
        this.transacaoMapper = transacaoMapper;
    }

    // ── CONSULTAS ─────────────────────────────────────────────────────────────

    public List<TransacaoDTO> listarPorConta(String numeroConta) {
        return transacaoRepository.findByNumeroContaOrderByDataHoraDesc(numeroConta)
                .stream().map(transacaoMapper::paraDTO).toList();
    }

    public Page<TransacaoDTO> listarPorContaPaginado(String numeroConta, int pagina, int tamanho) {
        return transacaoRepository.findByNumeroContaOrderByDataHoraDesc(
                numeroConta, PageRequest.of(pagina, tamanho))
                .map(transacaoMapper::paraDTO);
    }

    public List<TransacaoDTO> listarPorPeriodo(String numeroConta,
                                                LocalDateTime inicio, LocalDateTime fim) {
        return transacaoRepository
                .findByNumeroContaAndDataHoraBetweenOrderByDataHoraDesc(numeroConta, inicio, fim)
                .stream().map(transacaoMapper::paraDTO).toList();
    }

    public List<TransacaoDTO> listarPorTipo(String numeroConta, String tipo) {
        return transacaoRepository.findByNumeroContaAndTipoOrderByDataHoraDesc(numeroConta, tipo)
                .stream().map(transacaoMapper::paraDTO).toList();
    }

    // ── GERAÇÃO DE PDF ────────────────────────────────────────────────────────

    /**
     * Gera PDF completo do extrato de uma conta.
     * Layout profissional com cores, totalizadores e rodapé.
     */
    public byte[] gerarPdf(String numeroConta) {
        return gerarPdfPorPeriodo(numeroConta, null, null);
    }

    /**
     * Gera PDF do extrato filtrado por período.
     * Se inicio/fim forem nulos, usa todas as transações.
     */
    public byte[] gerarPdfPorPeriodo(String numeroConta,
                                      LocalDateTime inicio, LocalDateTime fim) {
        log.info("Gerando PDF do extrato — conta: {} | período: {} → {}",
                numeroConta,
                inicio != null ? inicio.format(FMT_DATE) : "início",
                fim    != null ? fim.format(FMT_DATE)    : "agora");

        List<TransacaoDTO> transacoes = (inicio != null && fim != null)
                ? listarPorPeriodo(numeroConta, inicio, fim)
                : listarPorConta(numeroConta);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try (PdfWriter writer    = new PdfWriter(baos);
             PdfDocument pdf     = new PdfDocument(writer);
             Document documento  = new Document(pdf)) {

            documento.setMargins(36, 36, 36, 36);

            // ── Cabeçalho principal ───────────────────────────
            adicionarCabecalho(documento, numeroConta, inicio, fim, transacoes.size());

            // ── Tabela de transações ──────────────────────────
            if (transacoes.isEmpty()) {
                documento.add(new Paragraph("Nenhuma transação encontrada para o período informado.")
                        .setTextAlignment(TextAlignment.CENTER)
                        .setFontColor(ColorConstants.GRAY)
                        .setMarginTop(20));
            } else {
                adicionarTabelaTransacoes(documento, transacoes);
                adicionarTotalizadores(documento, transacoes);
            }

            // ── Rodapé ────────────────────────────────────────
            adicionarRodape(documento);

        } catch (Exception ex) {
            log.error("Erro ao gerar PDF do extrato da conta {}: {}", numeroConta, ex.getMessage(), ex);
            throw new RuntimeException("Falha ao gerar extrato PDF: " + ex.getMessage(), ex);
        }

        log.info("PDF gerado com sucesso — {} transações — {} bytes",
                transacoes.size(), baos.size());
        return baos.toByteArray();
    }

    // ── MÉTODOS PRIVADOS DE COMPOSIÇÃO DO PDF ────────────────────────────────

    private void adicionarCabecalho(Document doc, String numeroConta,
                                     LocalDateTime inicio, LocalDateTime fim, int total) {
        // Bloco azul de título
        Table headerTable = new Table(UnitValue.createPercentArray(new float[]{1}))
                .setWidth(UnitValue.createPercentValue(100))
                .setBackgroundColor(COR_HEADER)
                .setBorder(Border.NO_BORDER);

        Cell tituloCell = new Cell()
                .add(new Paragraph("EXTRATO BANCÁRIO")
                        .setBold().setFontSize(20).setFontColor(ColorConstants.WHITE))
                .add(new Paragraph("BankSystem — Sistema Bancário Digital")
                        .setFontSize(10).setFontColor(new DeviceRgb(186, 212, 255)))
                .setBorder(Border.NO_BORDER)
                .setPadding(16);
        headerTable.addCell(tituloCell);
        doc.add(headerTable);

        // Dados da conta
        Table infoTable = new Table(UnitValue.createPercentArray(new float[]{1, 1}))
                .setWidth(UnitValue.createPercentValue(100))
                .setBackgroundColor(COR_SUBHEADER)
                .setBorder(new SolidBorder(COR_HEADER, 1))
                .setMarginBottom(16);

        infoTable.addCell(criarCelulaInfo("Conta", numeroConta));
        infoTable.addCell(criarCelulaInfo("Data de Geração", LocalDateTime.now().format(FMT_DATETIME)));

        String periodoTexto = (inicio != null && fim != null)
                ? inicio.format(FMT_DATE) + " a " + fim.format(FMT_DATE)
                : "Histórico completo";
        infoTable.addCell(criarCelulaInfo("Período", periodoTexto));
        infoTable.addCell(criarCelulaInfo("Total de Transações", String.valueOf(total)));

        doc.add(infoTable);
    }

    private void adicionarTabelaTransacoes(Document doc, List<TransacaoDTO> transacoes) {
        doc.add(new Paragraph("Lançamentos")
                .setBold().setFontSize(13).setMarginBottom(6));

        Table tabela = new Table(UnitValue.createPercentArray(new float[]{2.5f, 3f, 2.5f, 2.5f, 2f, 2f}))
                .setWidth(UnitValue.createPercentValue(100))
                .setMarginBottom(16);

        // Cabeçalho da tabela
        String[] cabecalhos = {"Data/Hora", "Descrição", "Tipo", "Valor (R$)", "Saldo Após", "Status"};
        for (String cab : cabecalhos) {
            tabela.addHeaderCell(new Cell()
                    .add(new Paragraph(cab).setBold().setFontSize(9).setFontColor(ColorConstants.WHITE))
                    .setBackgroundColor(COR_HEADER)
                    .setBorder(Border.NO_BORDER)
                    .setPadding(6));
        }

        // Linhas de dados
        for (int i = 0; i < transacoes.size(); i++) {
            TransacaoDTO t = transacoes.get(i);
            boolean isEntrada = isEntrada(t.tipo());
            DeviceRgb corLinha = isEntrada ? COR_ENTRADA
                    : (i % 2 == 0 ? COR_LINHA_PAR : new DeviceRgb(Color.WHITE));

            tabela.addCell(celulaTabela(
                    t.dataHora() != null ? t.dataHora().format(FMT_DATETIME) : "-", corLinha, false));
            tabela.addCell(celulaTabela(
                    t.descricao() != null ? t.descricao() : "-", corLinha, false));
            tabela.addCell(celulaTabela(
                    formatarTipo(t.tipo()), corLinha, false));
            tabela.addCell(celulaTabela(
                    "R$ " + (t.valor() != null ? t.valor().toPlainString() : "0.00"),
                    isEntrada ? COR_ENTRADA : COR_SAIDA, true));
            tabela.addCell(celulaTabela(
                    t.saldoAposTransacao() != null
                            ? "R$ " + t.saldoAposTransacao().toPlainString() : "-",
                    corLinha, false));
            tabela.addCell(celulaTabela(
                    t.status() != null ? t.status() : "-", corLinha, false));
        }

        doc.add(tabela);
    }

    private void adicionarTotalizadores(Document doc, List<TransacaoDTO> transacoes) {
        BigDecimal totalEntradas = transacoes.stream()
                .filter(t -> isEntrada(t.tipo()) && "APROVADA".equals(t.status()))
                .map(t -> t.valor() != null ? t.valor() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalSaidas = transacoes.stream()
                .filter(t -> !isEntrada(t.tipo()) && "APROVADA".equals(t.status()))
                .map(t -> t.valor() != null ? t.valor() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal saldoLiquido = totalEntradas.subtract(totalSaidas);

        BigDecimal saldoFinalConhecido = transacoes.stream()
                .map(TransacaoDTO::saldoAposTransacao)
                .filter(v -> v != null)
                .findFirst()
                .orElse(null);

        Table totais = new Table(UnitValue.createPercentArray(
                saldoFinalConhecido != null ? new float[]{1, 1, 1, 1} : new float[]{1, 1, 1}
        ))
                .setWidth(UnitValue.createPercentValue(100))
                .setMarginBottom(20);

        totais.addCell(criarCelulaTotalizador(
                "Total de Entradas",
                "R$ " + totalEntradas.toPlainString(),
                COR_ENTRADA
        ));

        totais.addCell(criarCelulaTotalizador(
                "Total de Saídas",
                "R$ " + totalSaidas.toPlainString(),
                COR_SAIDA
        ));

        totais.addCell(criarCelulaTotalizador(
                "Saldo do Período",
                "R$ " + saldoLiquido.toPlainString(),
                saldoLiquido.compareTo(BigDecimal.ZERO) >= 0 ? COR_ENTRADA : COR_SAIDA
        ));

        if (saldoFinalConhecido != null) {
            totais.addCell(criarCelulaTotalizador(
                    "Saldo Final",
                    "R$ " + saldoFinalConhecido.toPlainString(),
                    COR_SUBHEADER
            ));
        }

        doc.add(new Paragraph("Resumo do Período").setBold().setFontSize(13).setMarginBottom(6));
        doc.add(totais);
    }

    private void adicionarRodape(Document doc) {
        doc.add(new Paragraph(
                "Este documento é um extrato informativo gerado automaticamente pelo BankSystem em "
                + LocalDateTime.now().format(FMT_DATETIME) + ". "
                + "Guarde-o para sua conferência.")
                .setFontSize(8)
                .setFontColor(ColorConstants.GRAY)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginTop(10)
                .setBorderTop(new SolidBorder(ColorConstants.LIGHT_GRAY, 1))
                .setPaddingTop(8));
    }

    // ── HELPERS ───────────────────────────────────────────────────────────────

    private boolean isEntrada(String tipo) {
        return "DEPOSITO".equals(tipo)
                || "TRANSFERENCIA_ENTRADA".equals(tipo)
                || "ESTORNO_FRAUDE".equals(tipo);
    }

    private String formatarTipo(String tipo) {
        if (tipo == null) return "-";
        return switch (tipo) {
            case "DEPOSITO"              -> "Depósito";
            case "DEBITO"                -> "Débito";
            case "CREDITO"               -> "Crédito";
            case "TRANSFERENCIA_SAIDA"   -> "Transf. Envio";
            case "TRANSFERENCIA_ENTRADA" -> "Transf. Recebida";
            case "ESTORNO_FRAUDE"        -> "Estorno Fraude";
            default                      -> tipo;
        };
    }

    private Cell criarCelulaInfo(String rotulo, String valor) {
        return new Cell()
                .add(new Paragraph(rotulo).setFontSize(8).setFontColor(ColorConstants.GRAY))
                .add(new Paragraph(valor).setBold().setFontSize(11))
                .setBorder(Border.NO_BORDER)
                .setPadding(10);
    }

    private Cell celulaTabela(String texto, DeviceRgb cor, boolean negrito) {
        Paragraph p = new Paragraph(texto != null ? texto : "-").setFontSize(8);
        if (negrito) p.setBold();
        return new Cell().add(p)
                .setBackgroundColor(cor)
                .setBorder(Border.NO_BORDER)
                .setPadding(5);
    }

    private Cell criarCelulaTotalizador(String rotulo, String valor, DeviceRgb cor) {
        return new Cell()
                .add(new Paragraph(rotulo).setFontSize(9).setFontColor(ColorConstants.DARK_GRAY))
                .add(new Paragraph(valor).setBold().setFontSize(14))
                .setBackgroundColor(cor)
                .setBorder(new SolidBorder(ColorConstants.LIGHT_GRAY, 1))
                .setPadding(12)
                .setTextAlignment(TextAlignment.CENTER);
    }
}
