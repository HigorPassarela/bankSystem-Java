package br.com.banksystem.contas.repository;

import br.com.banksystem.contas.model.Conta;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

/**
 * Repositório de acesso a dados de contas bancárias.
 */
@Repository
public interface ContaRepository extends MongoRepository<Conta, String> {
    Optional<Conta> findByNumeroConta(String numeroConta);
    Optional<Conta> findByCpf(String cpf);
    Optional<Conta> findByEmail(String email);
    Optional<Conta> findByTokenVerificacaoEmail(String token);
    boolean existsByNumeroConta(String numeroConta);
    boolean existsByCpf(String cpf);
    boolean existsByEmail(String email);
}
