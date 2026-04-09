package br.com.banksystem.transacoes.client;

import br.com.banksystem.transacoes.dto.ValidacaoSenhaDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.*;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ContasClientTest {

    @Mock
    private RestTemplate restTemplate;

    private ContasClient contasClient;

    private final String contasUrl = "http://localhost:8081";
    private final String numeroConta = "12345678";
    private final String jwtToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.test";

    @BeforeEach
    void setUp() {
        contasClient = new ContasClient(restTemplate);
        ReflectionTestUtils.setField(contasClient, "contasUrl", contasUrl);
    }

    // ══════════════════════════════════════════════════════════════════════════
    // TESTES - VALIDAR SENHA TRANSFERÊNCIA
    // ══════════════════════════════════════════════════════════════════════════

    @Test
    void validarSenhaTransferencia_DeveRetornarTrue_QuandoSenhaValida() {
        // Arrange
        String senhaTransferencia = "1234";
        ValidacaoSenhaDTO validacaoResponse = new ValidacaoSenhaDTO(true, Boolean.TRUE, "Senha válida", null);
        ResponseEntity<ValidacaoSenhaDTO> responseEntity = new ResponseEntity<>(validacaoResponse, HttpStatus.OK);

        when(restTemplate.exchange(
                eq(contasUrl + "/api/contas/validar-senha-transferencia?senhaTransferencia=" + senhaTransferencia),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(ValidacaoSenhaDTO.class)
        )).thenReturn(responseEntity);

        // Act
        boolean resultado = contasClient.validarSenhaTransferencia(numeroConta, senhaTransferencia, jwtToken);

        // Assert
        assertTrue(resultado);

        verify(restTemplate).exchange(
                eq(contasUrl + "/api/contas/validar-senha-transferencia?senhaTransferencia=" + senhaTransferencia),
                eq(HttpMethod.POST),
                argThat(entity -> {
                    HttpHeaders headers = entity.getHeaders();
                    return headers.getFirst("Authorization").equals("Bearer " + jwtToken);
                }),
                eq(ValidacaoSenhaDTO.class)
        );
    }

    @Test
    void validarSenhaTransferencia_DeveRetornarFalse_QuandoSenhaInvalida() {
        // Arrange
        String senhaTransferencia = "0000";
        ValidacaoSenhaDTO validacaoResponse = new ValidacaoSenhaDTO(true, Boolean.FALSE, "Senha inválida", null);
        ResponseEntity<ValidacaoSenhaDTO> responseEntity = new ResponseEntity<>(validacaoResponse, HttpStatus.OK);

        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(ValidacaoSenhaDTO.class)
        )).thenReturn(responseEntity);

        // Act
        boolean resultado = contasClient.validarSenhaTransferencia(numeroConta, senhaTransferencia, jwtToken);

        // Assert
        assertFalse(resultado);
    }

    @Test
    void validarSenhaTransferencia_DeveRetornarFalse_QuandoResponseBodyNulo() {
        // Arrange
        String senhaTransferencia = "1234";
        ResponseEntity<ValidacaoSenhaDTO> responseEntity = new ResponseEntity<>(null, HttpStatus.OK);

        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(ValidacaoSenhaDTO.class)
        )).thenReturn(responseEntity);

        // Act
        boolean resultado = contasClient.validarSenhaTransferencia(numeroConta, senhaTransferencia, jwtToken);

        // Assert
        assertFalse(resultado);
    }

    @Test
    void validarSenhaTransferencia_DeveRetornarFalse_QuandoStatusNaoSucesso() {
        // Arrange
        String senhaTransferencia = "1234";
        ValidacaoSenhaDTO validacaoResponse = new ValidacaoSenhaDTO(false, null, "Erro interno", null);
        ResponseEntity<ValidacaoSenhaDTO> responseEntity = new ResponseEntity<>(validacaoResponse, HttpStatus.INTERNAL_SERVER_ERROR);

        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(ValidacaoSenhaDTO.class)
        )).thenReturn(responseEntity);

        // Act
        boolean resultado = contasClient.validarSenhaTransferencia(numeroConta, senhaTransferencia, jwtToken);

        // Assert
        assertFalse(resultado);
    }

    @Test
    void validarSenhaTransferencia_DeveRetornarFalse_QuandoDadosNaoBoolean() {
        // Arrange
        String senhaTransferencia = "1234";
        ValidacaoSenhaDTO validacaoResponse = new ValidacaoSenhaDTO(true, "string-instead-of-boolean", "Resposta inválida", null);
        ResponseEntity<ValidacaoSenhaDTO> responseEntity = new ResponseEntity<>(validacaoResponse, HttpStatus.OK);

        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(ValidacaoSenhaDTO.class)
        )).thenReturn(responseEntity);

        // Act
        boolean resultado = contasClient.validarSenhaTransferencia(numeroConta, senhaTransferencia, jwtToken);

        // Assert
        assertFalse(resultado);
    }

    @Test
    void validarSenhaTransferencia_DeveRetornarFalse_QuandoOcorreExcecao() {
        // Arrange
        String senhaTransferencia = "1234";

        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(ValidacaoSenhaDTO.class)
        )).thenThrow(new RestClientException("Erro de conectividade"));

        // Act
        boolean resultado = contasClient.validarSenhaTransferencia(numeroConta, senhaTransferencia, jwtToken);

        // Assert
        assertFalse(resultado);
    }

    // ══════════════════════════════════════════════════════════════════════════
    // TESTES - BUSCAR NOME CONTA
    // ══════════════════════════════════════════════════════════════════════════

    @Test
    void buscarNomeConta_DeveRetornarNomeCompleto_QuandoContaEncontrada() {
        // Arrange
        String nomeEsperado = "João da Silva Santos";
        Map<String, Object> dadosMap = new HashMap<>();
        dadosMap.put("nomeCompleto", nomeEsperado);
        dadosMap.put("numeroConta", numeroConta);

        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put("dados", dadosMap);

        ResponseEntity<Map> responseEntity = new ResponseEntity<>(responseMap, HttpStatus.OK);

        when(restTemplate.exchange(
                eq(contasUrl + "/api/contas/buscar/" + numeroConta),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(Map.class)
        )).thenReturn(responseEntity);

        // Act
        String resultado = contasClient.buscarNomeConta(numeroConta, jwtToken);

        // Assert
        assertEquals(nomeEsperado, resultado);

        verify(restTemplate).exchange(
                eq(contasUrl + "/api/contas/buscar/" + numeroConta),
                eq(HttpMethod.GET),
                argThat(entity -> {
                    HttpHeaders headers = entity.getHeaders();
                    return headers.getFirst("Authorization").equals("Bearer " + jwtToken);
                }),
                eq(Map.class)
        );
    }

    @Test
    void buscarNomeConta_DeveRetornarNumeroConta_QuandoNomeCompletoNulo() {
        // Arrange
        Map<String, Object> dadosMap = new HashMap<>();
        dadosMap.put("nomeCompleto", null);
        dadosMap.put("numeroConta", numeroConta);

        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put("dados", dadosMap);

        ResponseEntity<Map> responseEntity = new ResponseEntity<>(responseMap, HttpStatus.OK);

        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(Map.class)
        )).thenReturn(responseEntity);

        // Act
        String resultado = contasClient.buscarNomeConta(numeroConta, jwtToken);

        // Assert
        assertEquals(numeroConta, resultado);
    }

    @Test
    void buscarNomeConta_DeveRetornarStringVazia_QuandoNomeCompletoVazio() {
        // Arrange
        String nomeVazio = "";
        Map<String, Object> dadosMap = new HashMap<>();
        dadosMap.put("nomeCompleto", nomeVazio);
        dadosMap.put("numeroConta", numeroConta);

        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put("dados", dadosMap);

        ResponseEntity<Map> responseEntity = new ResponseEntity<>(responseMap, HttpStatus.OK);

        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(Map.class)
        )).thenReturn(responseEntity);

        // Act
        String resultado = contasClient.buscarNomeConta(numeroConta, jwtToken);

        // Assert
        assertEquals(nomeVazio, resultado); // Retorna string vazia, não o número da conta
    }

    @Test
    void buscarNomeConta_DeveRetornarNumeroConta_QuandoResponseBodyNulo() {
        // Arrange
        ResponseEntity<Map> responseEntity = new ResponseEntity<>(null, HttpStatus.OK);

        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(Map.class)
        )).thenReturn(responseEntity);

        // Act
        String resultado = contasClient.buscarNomeConta(numeroConta, jwtToken);

        // Assert
        assertEquals(numeroConta, resultado);
    }

    @Test
    void buscarNomeConta_DeveRetornarNumeroConta_QuandoDadosNaoEhMap() {
        // Arrange
        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put("dados", "string-instead-of-map");

        ResponseEntity<Map> responseEntity = new ResponseEntity<>(responseMap, HttpStatus.OK);

        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(Map.class)
        )).thenReturn(responseEntity);

        // Act
        String resultado = contasClient.buscarNomeConta(numeroConta, jwtToken);

        // Assert
        assertEquals(numeroConta, resultado);
    }

    @Test
    void buscarNomeConta_DeveRetornarNumeroConta_QuandoDadosNulo() {
        // Arrange
        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put("dados", null);

        ResponseEntity<Map> responseEntity = new ResponseEntity<>(responseMap, HttpStatus.OK);

        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(Map.class)
        )).thenReturn(responseEntity);

        // Act
        String resultado = contasClient.buscarNomeConta(numeroConta, jwtToken);

        // Assert
        assertEquals(numeroConta, resultado);
    }

    @Test
    void buscarNomeConta_DeveRetornarNumeroConta_QuandoStatusNaoSucesso() {
        // Arrange
        ResponseEntity<Map> responseEntity = new ResponseEntity<>(new HashMap<>(), HttpStatus.NOT_FOUND);

        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(Map.class)
        )).thenReturn(responseEntity);

        // Act
        String resultado = contasClient.buscarNomeConta(numeroConta, jwtToken);

        // Assert
        assertEquals(numeroConta, resultado);
    }

    @Test
    void buscarNomeConta_DeveRetornarNumeroConta_QuandoOcorreExcecao() {
        // Arrange
        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(Map.class)
        )).thenThrow(new RestClientException("Serviço indisponível"));

        // Act
        String resultado = contasClient.buscarNomeConta(numeroConta, jwtToken);

        // Assert
        assertEquals(numeroConta, resultado);
    }

    @Test
    void buscarNomeConta_DeveConverterObjetoParaString_QuandoNomeEhNumero() {
        // Arrange
        Map<String, Object> dadosMap = new HashMap<>();
        dadosMap.put("nomeCompleto", 12345); // Número em vez de string

        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put("dados", dadosMap);

        ResponseEntity<Map> responseEntity = new ResponseEntity<>(responseMap, HttpStatus.OK);

        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(Map.class)
        )).thenReturn(responseEntity);

        // Act
        String resultado = contasClient.buscarNomeConta(numeroConta, jwtToken);

        // Assert
        assertEquals("12345", resultado);
    }

    @Test
    void buscarNomeConta_DeveRetornarNumeroConta_QuandoMapDadosVazio() {
        // Arrange
        Map<String, Object> dadosMap = new HashMap<>();
        // Map vazio, sem a chave "nomeCompleto"

        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put("dados", dadosMap);

        ResponseEntity<Map> responseEntity = new ResponseEntity<>(responseMap, HttpStatus.OK);

        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(Map.class)
        )).thenReturn(responseEntity);

        // Act
        String resultado = contasClient.buscarNomeConta(numeroConta, jwtToken);

        // Assert
        assertEquals(numeroConta, resultado);
    }
}