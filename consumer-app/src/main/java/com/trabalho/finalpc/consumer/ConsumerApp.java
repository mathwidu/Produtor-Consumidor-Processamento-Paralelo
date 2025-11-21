package com.trabalho.finalpc.consumer; // Pacote da aplicação consumidora

import com.trabalho.finalpc.common.config.AppConfig; // Lê CONSUMER_ID e dados do broker via ENV
import com.trabalho.finalpc.consumer.messaging.NoOpSubscriber; // Adaptador que simula recebimento
import com.trabalho.finalpc.consumer.messaging.RabbitSubscriber; // Adaptador que consome do RabbitMQ
import org.slf4j.Logger;        // Interface de logging (SLF4J)
import org.slf4j.LoggerFactory; // Fábrica de loggers (um por classe)

/**
 * Esta é a classe main do CONSUMIDOR. Aqui eu:
 * - Leio CONSUMER_ID (via AppConfig/ENV) para diferenciar processos.
 * - Escolho o adaptador de mensageria (Rabbit/NoOp) conforme USE_RABBIT.
 * - Monto o serviço com a estratégia aleatória de “necessidade de tipo”.
 * - Rodo N iterações (ITERATIONS) para facilitar a demonstração.
 */
public class ConsumerApp { // Classe principal do módulo consumidor
    private static final Logger log = LoggerFactory.getLogger(ConsumerApp.class); // Logger da aplicação

    public static void main(String[] args) { // Método main: ponto de entrada da JVM
        // Log inicial para confirmar que o serviço iniciou
        log.info("ConsumerApp inicializado");

        // Resolução e exibição do ID do consumidor (para testes e rastreio)
        String consumerId = AppConfig.getConsumerId();
        log.info("Consumer ID resolvido: {}", consumerId);

        // Decide adaptador (Rabbit/NoOp), cria o serviço e seleciona a estratégia
        var subscriber = tryCreateRabbitOrNoOp();
        var strategy = new RandomNeedSelectionStrategy();
        var service = new ConsumerService(consumerId, subscriber, strategy);

        int iterations = 5; // padrão para testes curtos (configurável por ENV)
        String iterEnv = System.getenv("ITERATIONS");
        if (iterEnv != null) {
            try { iterations = Math.max(1, Integer.parseInt(iterEnv.trim())); } catch (NumberFormatException ignored) {}
        }

        Runtime.getRuntime().addShutdownHook(new Thread(() -> { // Fecha recursos no encerramento
            try { subscriber.close(); } catch (Exception ignored) {}
        }));

        service.runLoop(iterations);
    }

    private static com.trabalho.finalpc.common.messaging.MessageSubscriber tryCreateRabbitOrNoOp() {
        String useRabbit = System.getenv("USE_RABBIT"); // Se TRUE, tentamos usar o adaptador Rabbit
        if (useRabbit != null && useRabbit.equalsIgnoreCase("true")) {
            try {
                return new RabbitSubscriber(); // Conecta, declara topologia e retorna adaptador real
            } catch (Exception e) {
                LoggerFactory.getLogger(ConsumerApp.class)
                        .warn("Falha ao iniciar RabbitMQ ({}). Usando NoOpSubscriber.", e.toString()); // Fallback seguro
            }
        }
        return new NoOpSubscriber(); // Caminho padrão: simular recebimento
    }
}
