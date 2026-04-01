package br.com.banksystem.transacoes.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

/**
 * Configuração dos tópicos Kafka para transações.
 */
@Configuration
public class KafkaConfig {

    @Bean
    public NewTopic topicoTransacoesAprovadas() {
        return TopicBuilder.name("transacoes-aprovadas").partitions(3).replicas(1).build();
    }

    @Bean
    public NewTopic topicoTransacoesReprovadas() {
        return TopicBuilder.name("transacoes-reprovadas").partitions(3).replicas(1).build();
    }
}
