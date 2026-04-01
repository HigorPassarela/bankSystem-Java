package br.com.banksystem.contas.security;

import br.com.banksystem.contas.model.Conta;
import br.com.banksystem.contas.repository.ContaRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Serviço de carregamento de detalhes do usuário para Spring Security.
 * Utiliza o número da conta como identificador de login.
 */
@Service
public class ContaUserDetailsService implements UserDetailsService {

    private final ContaRepository contaRepository;

    public ContaUserDetailsService(ContaRepository contaRepository) {
        this.contaRepository = contaRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String numeroConta) throws UsernameNotFoundException {
        Conta conta = contaRepository.findByNumeroConta(numeroConta)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "Conta não encontrada: " + numeroConta));

        if (!conta.getAtiva()) {
            throw new UsernameNotFoundException("Conta inativa: " + numeroConta);
        }

        return new User(
                conta.getNumeroConta(),
                conta.getSenhaHash(),
                List.of(new SimpleGrantedAuthority(conta.getRole()))
        );
    }
}
