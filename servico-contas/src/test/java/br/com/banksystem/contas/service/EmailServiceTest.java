package br.com.banksystem.contas.service;

import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    private EmailService emailService;
    private MimeMessage mimeMessage;

    @BeforeEach
    void setUp() {
        emailService = new EmailService(mailSender);
        ReflectionTestUtils.setField(emailService, "urlBase", "http://localhost:8081");

        Session session = Session.getDefaultInstance(new Properties());
        mimeMessage = new MimeMessage(session);
    }

    @Test
    void deveEnviarEmailDeVerificacaoComSucesso() {
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        assertDoesNotThrow(() ->
                emailService.enviarVerificacaoEmail(
                        "joao@email.com",
                        "João Silva",
                        "12345678",
                        "token123"
                )
        );

        verify(mailSender).createMimeMessage();
        verify(mailSender).send(mimeMessage);
    }

    @Test
    void deveLancarRuntimeExceptionQuandoFalharAoEnviarEmailDeVerificacao() {
        when(mailSender.createMimeMessage()).thenThrow(new RuntimeException("Falha no mail sender"));

        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> emailService.enviarVerificacaoEmail(
                        "joao@email.com",
                        "João Silva",
                        "12345678",
                        "token123"
                )
        );

        assertTrue(ex.getMessage().contains("Falha no mail sender"));
        verify(mailSender).createMimeMessage();
        verify(mailSender, never()).send(any(MimeMessage.class));
    }

    @Test
    void deveEnviarNotificacaoDeTransferenciaComSucesso() {
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        assertDoesNotThrow(() ->
                emailService.enviarNotificacaoTransferencia(
                        "maria@email.com",
                        "Maria Souza",
                        "87654321",
                        "150,00",
                        "João Silva"
                )
        );

        verify(mailSender).createMimeMessage();
        verify(mailSender).send(mimeMessage);
    }

    @Test
    void devePropagarRuntimeExceptionQuandoFalharAoEnviarNotificacaoDeTransferencia() {
        when(mailSender.createMimeMessage()).thenThrow(new RuntimeException("Erro no envio"));

        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> emailService.enviarNotificacaoTransferencia(
                        "maria@email.com",
                        "Maria Souza",
                        "87654321",
                        "150,00",
                        "João Silva"
                )
        );

        assertEquals("Erro no envio", ex.getMessage());
        verify(mailSender).createMimeMessage();
        verify(mailSender, never()).send(any(MimeMessage.class));
    }
}