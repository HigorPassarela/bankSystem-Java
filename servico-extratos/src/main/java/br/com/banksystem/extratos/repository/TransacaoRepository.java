package br.com.banksystem.extratos.repository;

import br.com.banksystem.extratos.model.Transacao;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repositório de transações no MongoDB.
 * Todos os filtros usam os índices compostos definidos na entidade Transacao.
 */
@Repository
public interface TransacaoRepository extends MongoRepository<Transacao, String> {

    /**
     * Todas as transações de uma conta, mais recentes primeiro
     */
    List<Transacao> findByNumeroContaOrderByDataHoraDesc(String numeroConta);

    /**
     * Paginado — para listagens grandes
     */
    Page<Transacao> findByNumeroContaOrderByDataHoraDesc(String numeroConta, Pageable pageable);

    /**
     * Filtro por período
     */
    List<Transacao> findByNumeroContaAndDataHoraBetweenOrderByDataHoraDesc(
            String numeroConta, LocalDateTime inicio, LocalDateTime fim);

    /**
     * Filtro por tipo (ex: DEPOSITO, TRANSFERENCIA_ENTRADA)
     */
    List<Transacao> findByNumeroContaAndTipoOrderByDataHoraDesc(String numeroConta, String tipo);

    /**
     * Filtro por tipo e período
     */
    List<Transacao> findByNumeroContaAndTipoAndDataHoraBetweenOrderByDataHoraDesc(
            String numeroConta, String tipo, LocalDateTime inicio, LocalDateTime fim);

    /**
     * Buscar pelo ID da transação (para evitar duplicatas no consumidor Kafka)
     */
    Optional<Transacao> findByIdTransacao(String idTransacao);

    /**
     * Verificar se transação já foi processada (idempotência)
     */
    boolean existsByIdTransacao(String idTransacao);

    /**
     * Contar transações por status de uma conta
     */
    long countByNumeroContaAndStatus(String numeroConta, String status);
}
