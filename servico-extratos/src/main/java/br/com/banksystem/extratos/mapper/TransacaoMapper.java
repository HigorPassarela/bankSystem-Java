package br.com.banksystem.extratos.mapper;

import br.com.banksystem.extratos.dto.TransacaoDTO;
import br.com.banksystem.extratos.dto.TransacaoEventoDTO;
import br.com.banksystem.extratos.model.Transacao;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Mapper manual entre Transacao (entidade) e seus DTOs.
 */
@Component
public class TransacaoMapper {

    public Transacao paraEntidade(TransacaoEventoDTO dto) {
        Transacao t = new Transacao();
        t.setIdTransacao(dto.idTransacao());
        t.setNumeroConta(dto.numeroConta());
        t.setValor(dto.valor());
        t.setTipo(dto.tipo());
        t.setStatus(dto.status());
        t.setDescricao(dto.descricao());
        t.setDataHora(dto.dataHora() != null ? dto.dataHora() : LocalDateTime.now());
        t.setSaldoAposTransacao(dto.saldoAposTransacao());
        return t;
    }

    public TransacaoDTO paraDTO(Transacao t) {
        return new TransacaoDTO(
                t.getIdTransacao(), t.getNumeroConta(), t.getValor(),
                t.getTipo(), t.getStatus(), t.getDescricao(),
                t.getDataHora(), t.getSaldoAposTransacao()
        );
    }
}
