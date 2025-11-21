package com.trabalho.finalpc.producer; // Pacote da aplicação produtora (organiza as classes do produtor)

import com.trabalho.finalpc.common.config.AppConfig; // Para resolver IDs e configs via variáveis de ambiente
import com.trabalho.finalpc.producer.messaging.NoOpPublisher; // Adaptador que apenas loga (sem broker)
import com.trabalho.finalpc.producer.messaging.RabbitPublisher; // Adaptador real de RabbitMQ
import org.slf4j.Logger;        // Interface de logging (SLF4J)
import org.slf4j.LoggerFactory; // Fábrica para obter um logger por classe

/**
 * Esta é a classe main do PRODUTOR. Aqui eu:
 * - Leio PRODUCER_ID via AppConfig (ou derivo do hostname).
 * - Escolho qual adaptador de mensageria usar (RabbitMQ real ou NoOp para simulação).
 * - Monto o serviço (ProducerService) com a estratégia aleatória de tipos.
 * - Rodo o loop por N iterações (ITERATIONS), útil para a demonstração no vídeo.
 */
public class ProducerApp {
    private static final Logger log = LoggerFactory.getLogger(ProducerApp.class); // Logger da aplicação

    public static void main(String[] args) {
        log.info("ProducerApp inicializado"); // Sinaliza subida

        String producerId = AppConfig.getProducerId(); // Resolve o ID do produtor
        log.info("Producer ID resolvido: {}", producerId); // Exibe quem somos nos logs

        var publisher = tryCreateRabbitOrNoOp(); // Rabbit se USE_RABBIT=true e conexão ok; senão NoOp
        var strategy = new RandomTypeSelectionStrategy(); // Seleciona TIPO_A/TIPO_B aleatoriamente
        var service = new ProducerService(producerId, publisher, strategy); // Serviço com dependências injetadas

        int iterations = 5; // Valor padrão para apresentações curtas
        String iterEnv = System.getenv("ITERATIONS"); // Permite configurar via ENV
        if (iterEnv != null) { // Se definido, tentamos converter
            try { iterations = Math.max(1, Integer.parseInt(iterEnv.trim())); } catch (NumberFormatException ignored) {}
        }

        Runtime.getRuntime().addShutdownHook(new Thread(() -> { // Hook para fechamento limpo
            try { publisher.close(); } catch (Exception ignored) {}
        }));

        service.runLoop(iterations); // Inicia o ciclo de produção
    }

    // Decide qual adaptador de publicação usar (Rabbit ou NoOp), com fallback seguro.
    private static com.trabalho.finalpc.common.messaging.MessagePublisher tryCreateRabbitOrNoOp() {
        String useRabbit = System.getenv("USE_RABBIT"); // Flag de comutação
        if (useRabbit != null && useRabbit.equalsIgnoreCase("true")) { // Caso queira RabbitMQ
            try {
                return new RabbitPublisher(); // Tenta abrir conexão e declarar topologia
            } catch (Exception e) { // Se der erro (ex.: broker offline)
                LoggerFactory.getLogger(ProducerApp.class)
                        .warn("Falha ao iniciar RabbitMQ ({}). Usando NoOpPublisher.", e.toString());
            }
        }
        return new NoOpPublisher(); // Caminho seguro: apenas loga
    }
}
