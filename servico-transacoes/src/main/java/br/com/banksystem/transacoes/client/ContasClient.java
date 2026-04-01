package br.com.banksystem.transacoes.client;

import br.com.banksystem.transacoes.dto.ValidacaoSenhaDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * Cliente HTTP para comunicação com o Serviço de Contas.
 * Usado para validar a senha de transferência e buscar dados de contas.
 */
@Component
public class ContasClient {

    private static final Logger log = LoggerFactory.getLogger(ContasClient.class);

    private final RestTemplate restTemplate;

    @Value("${servico.contas.url:http://localhost:8081}")
    private String contasUrl;

    public ContasClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Valida o PIN de transferência da conta junto ao servico-contas.
     */
    public boolean validarSenhaTransferencia(String numeroConta, String senhaTransferencia, String jwtToken) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(jwtToken);

            HttpEntity<Void> entity = new HttpEntity<>(headers);
            String url = contasUrl + "/api/contas/validar-senha-transferencia?senhaTransferencia=" + senhaTransferencia;

            ResponseEntity<ValidacaoSenhaDTO> resposta = restTemplate.exchange(
                    url, HttpMethod.POST, entity, ValidacaoSenhaDTO.class);

            if (resposta.getStatusCode().is2xxSuccessful() && resposta.getBody() != null) {
                Object dados = resposta.getBody().dados();
                return Boolean.TRUE.equals(dados);
            }
            return false;
        } catch (Exception ex) {
            log.error("Erro ao validar senha de transferência para conta {}: {}", numeroConta, ex.getMessage());
            return false;
        }
    }

    /**
     * Busca o nome do titular da conta de destino para registro.
     */
    public String buscarNomeConta(String numeroConta, String jwtToken) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(jwtToken);
            HttpEntity<Void> entity = new HttpEntity<>(headers);
            String url = contasUrl + "/api/contas/buscar/" + numeroConta;

            ResponseEntity<java.util.Map> resposta = restTemplate.exchange(
                    url, HttpMethod.GET, entity, java.util.Map.class);

            if (resposta.getStatusCode().is2xxSuccessful() && resposta.getBody() != null) {
                Object dados = resposta.getBody().get("dados");
                if (dados instanceof java.util.Map<?,?> dadosMap) {
                    Object nome = dadosMap.get("nomeCompleto");
                    return nome != null ? nome.toString() : numeroConta;
                }
            }
            return numeroConta;
        } catch (Exception ex) {
            log.warn("Não foi possível buscar nome da conta {}: {}", numeroConta, ex.getMessage());
            return numeroConta;
        }
    }
}
