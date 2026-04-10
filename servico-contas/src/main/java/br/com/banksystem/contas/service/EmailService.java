package br.com.banksystem.contas.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

/**
 * Serviço responsável pelo envio de e-mails transacionais via MailHog (dev) ou SMTP real (prod).
 */
@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;

    @Value("${app.url-base}")
    private String urlBase;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    /**
     * Envia e-mail de verificação com link de ativação da conta.
     */
    public void enviarVerificacaoEmail(String destinatario, String nomeCompleto,
                                       String numeroConta, String token) {
        try {
            MimeMessage mensagem = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mensagem, true, "UTF-8");

            helper.setFrom("noreply@banksystem.com.br");
            helper.setTo(destinatario);
            helper.setSubject("BankSystem — Verifique seu e-mail para ativar sua conta");

            String linkVerificacao = urlBase + "/api/contas/verificar-email?token=" + token;
            String html = """
                    <html><body style="font-family: Arial, sans-serif; max-width: 600px; margin: auto;">
                    <div style="background: #1a56db; padding: 20px; border-radius: 8px 8px 0 0;">
                      <h1 style="color: white; margin: 0;">BankSystem</h1>
                    </div>
                    <div style="background: #f9fafb; padding: 30px; border-radius: 0 0 8px 8px; border: 1px solid #e5e7eb;">
                      <h2 style="color: #111827;">Olá, %s!</h2>
                      <p style="color: #374151;">Sua conta foi criada com sucesso.</p>
                      <p style="color: #374151;"><strong>Número da conta:</strong> %s</p>
                      <p style="color: #374151;">Para ativar sua conta e ter acesso completo ao sistema, clique no botão abaixo:</p>
                      <div style="text-align: center; margin: 30px 0;">
                        <a href="%s" style="background: #1a56db; color: white; padding: 14px 28px;
                           border-radius: 6px; text-decoration: none; font-size: 16px; font-weight: bold;">
                          ✅ Verificar E-mail e Ativar Conta
                        </a>
                      </div>
                      <p style="color: #6b7280; font-size: 14px;">Este link expira em 24 horas.</p>
                      <p style="color: #6b7280; font-size: 14px;">Se você não criou esta conta, ignore este e-mail.</p>
                      <hr style="border: none; border-top: 1px solid #e5e7eb; margin: 20px 0;">
                      <p style="color: #9ca3af; font-size: 12px; text-align: center;">© 2024 BankSystem. Todos os direitos reservados.</p>
                    </div>
                    </body></html>
                    """.formatted(nomeCompleto, numeroConta, linkVerificacao);

            helper.setText(html, true);
            mailSender.send(mensagem);
            log.info("E-mail de verificação enviado para: {}", destinatario);
        } catch (MessagingException ex) {
            log.error("Erro ao enviar e-mail de verificação para {}: {}", destinatario, ex.getMessage());
            throw new RuntimeException("Falha ao enviar e-mail de verificação", ex);
        }
    }

    /**
     * Envia e-mail de notificação de transferência recebida.
     */
    public void enviarNotificacaoTransferencia(String destinatario, String nomeDestinatario,
                                               String numeroConta, String valorFormatado,
                                               String nomeRemetente) {
        try {
            MimeMessage mensagem = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mensagem, true, "UTF-8");

            helper.setFrom("noreply@banksystem.com.br");
            helper.setTo(destinatario);
            helper.setSubject("BankSystem — Você recebeu uma transferência de R$ " + valorFormatado);

            String html = """
                    <html><body style="font-family: Arial, sans-serif; max-width: 600px; margin: auto;">
                    <div style="background: #059669; padding: 20px; border-radius: 8px 8px 0 0;">
                      <h1 style="color: white; margin: 0;">💰 Transferência Recebida</h1>
                    </div>
                    <div style="background: #f9fafb; padding: 30px; border-radius: 0 0 8px 8px; border: 1px solid #e5e7eb;">
                      <h2 style="color: #111827;">Olá, %s!</h2>
                      <p style="color: #374151;">Você recebeu uma transferência em sua conta <strong>%s</strong>.</p>
                      <div style="background: #d1fae5; border: 1px solid #6ee7b7; border-radius: 8px; padding: 20px; text-align: center; margin: 20px 0;">
                        <p style="color: #065f46; font-size: 14px; margin: 0;">Valor recebido</p>
                        <p style="color: #065f46; font-size: 32px; font-weight: bold; margin: 8px 0;">R$ %s</p>
                        <p style="color: #065f46; font-size: 14px; margin: 0;">De: %s</p>
                      </div>
                      <p style="color: #6b7280; font-size: 14px;">Acesse o app para ver seu extrato atualizado.</p>
                    </div>
                    </body></html>
                    """.formatted(nomeDestinatario, numeroConta, valorFormatado, nomeRemetente);

            helper.setText(html, true);
            mailSender.send(mensagem);
            log.info("Notificação de transferência enviada para: {}", destinatario);
        } catch (MessagingException ex) {
            log.error("Erro ao enviar notificação de transferência: {}", ex.getMessage());
        }
    }
}
