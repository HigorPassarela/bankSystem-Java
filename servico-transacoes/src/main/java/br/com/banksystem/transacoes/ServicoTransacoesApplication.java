package br.com.banksystem.transacoes;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Classe principal do Serviço de Transações.
 * Responsável pelo processamento de crédito e débito.
 */
@SpringBootApplication
public class ServicoTransacoesApplication {
    public static void main(String[] args) {
        SpringApplication.run(ServicoTransacoesApplication.class, args);
    }
}
