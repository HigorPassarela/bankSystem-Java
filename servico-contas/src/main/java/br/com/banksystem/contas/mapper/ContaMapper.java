package br.com.banksystem.contas.mapper;

import br.com.banksystem.contas.dto.CriarContaDTO;
import br.com.banksystem.contas.dto.PerfilContaDTO;
import br.com.banksystem.contas.model.Conta;
import br.com.banksystem.contas.model.enums.StatusConta;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Mapper manual — converte entre entidade Conta e seus DTOs.
 */
@Component
public class ContaMapper {

    public Conta paraEntidade(CriarContaDTO dto, String senhaHash,
                              String senhaTransferenciaHash, String numeroConta) {
        Conta conta = new Conta();
        conta.setNumeroConta(numeroConta);
        conta.setNomeCompleto(dto.nomeCompleto());
        conta.setCpf(dto.cpf());
        conta.setEmail(dto.email());
        conta.setTelefone(dto.telefone());
        conta.setSenhaHash(senhaHash);
        conta.setSenhaTransferenciaHash(senhaTransferenciaHash);
        conta.setRole("ROLE_USUARIO");
        conta.setStatus(StatusConta.PENDENTE_EMAIL);   // começa pendente
        conta.setEmailVerificado(false);
        conta.setDataCriacao(LocalDateTime.now());
        conta.setDataAtualizacao(LocalDateTime.now());
        return conta;
    }

    public PerfilContaDTO paraPerfilDTO(Conta conta) {
        return new PerfilContaDTO(
                conta.getNumeroConta(),
                conta.getNomeCompleto(),
                conta.getCpf(),
                conta.getEmail(),
                conta.getTelefone(),
                conta.getStatus(),
                conta.getAtiva(),
                conta.getEmailVerificado(),
                conta.getDataCriacao()
        );
    }
}
