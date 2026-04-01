package br.com.banksystem.contas;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Classe principal do Serviço de Contas.
 * Responsável por gerenciamento e autenticação de contas bancárias.
 */
@SpringBootApplication
public class ServicoContasApplication {

    public static void main(String[] args) {
        SpringApplication.run(ServicoContasApplication.class, args);
    }
}
