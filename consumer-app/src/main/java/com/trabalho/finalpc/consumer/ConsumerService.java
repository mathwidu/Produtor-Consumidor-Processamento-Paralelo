package com.trabalho.finalpc.consumer; // Pacote da aplicação consumidora

import com.trabalho.finalpc.common.ProductMessage;               // DTO recebido do broker (payload)
import com.trabalho.finalpc.common.ProductType;                  // Tipo de produto que o consumidor deseja
import com.trabalho.finalpc.common.Timing;                       // Tempos de consumo (dobro da produção)
import com.trabalho.finalpc.common.messaging.MessageSubscriber;  // Porta/contrato para obter mensagens do broker
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Locale;                                        // Para formatar segundos com ponto decimal

/**
 * Este serviço encapsula o ciclo do consumidor:
 * 1) Eu escolho um tipo “necessário” (estratégia aleatória)
 * 2) Eu recebo 1 mensagem da fila correspondente (MessageSubscriber)
 * 3) Eu simulo o consumo com o tempo correto (Timing)
 * Observação: eu uso basicGet (pull) para consumir exatamente 1 do tipo escolhido por iteração.
 */
public class ConsumerService { // Classe de serviço (stateless)

    private static final Logger log = LoggerFactory.getLogger(ConsumerService.class);

    private final String consumerId;                // Quem somos (para logs e auditoria)
    private final MessageSubscriber subscriber;     // Adaptador para receber mensagens (Rabbit/NoOp)
    private final NeedSelectionStrategy needStrategy; // Política para escolher o tipo necessário

    /** Construtor com dependências injetadas. */
    public ConsumerService(String consumerId,
                           MessageSubscriber subscriber,
                           NeedSelectionStrategy needStrategy) {
        this.consumerId = consumerId;          // Armazena ID do consumidor
        this.subscriber = subscriber;          // Armazena o assinante de mensageria
        this.needStrategy = needStrategy;      // Armazena a estratégia de necessidade
    }

    /** Executa N iterações do ciclo de consumo. */
    public void runLoop(int iterations) {
        for (int i = 1; i <= iterations; i++) {
            ProductType need = needStrategy.nextNeededType(); // 1) Escolhe tipo necessário (A/B)
            long consMs = Timing.consumptionMillis(need);     // 2) Calcula tempo de consumo (dobro de produção)
            try {
                log.info("[Consumer {}] Iteração {} => precisa tipo={}, aguardando mensagem...", consumerId, i, need);
                ProductMessage msg = subscriber.receiveOne(need); // 2) Recebe 1 mensagem da fila do tipo escolhido
                log.info("[Consumer {}] Recebido itemId={} tipo={}. Consumindo por {} ({} ms)...",
                        consumerId, msg.getItemId(), msg.getType(), fmtSeconds(consMs), consMs);
                Thread.sleep(consMs); // 3) Simula processamento do item
                log.info("[Consumer {}] Consumo concluído itemId={} tipo={}", consumerId, msg.getItemId(), msg.getType());
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                log.warn("[Consumer {}] Interrompido durante consumo.", consumerId);
                break;
            } catch (Exception e) {
                log.error("[Consumer {}] Falha ao receber/consumir: {}", consumerId, e.getMessage(), e);
            }
        }
    }

    /** Processa uma única mensagem (unidade de trabalho), dado um tipo desejado. */
    ProductMessage consumeOne(ProductType neededType) {
        // Método auxiliar se quisermos isolar a unidade de trabalho (não usado neste loop mínimo).
        throw new UnsupportedOperationException("Receber 1 mensagem do tipo, simular consumo e ack");
    }

    // Helper: 7000 ms -> "7.0 s" (formato amigável para o vídeo)
    private static String fmtSeconds(long ms) {
        return String.format(Locale.US, "%.1f s", ms / 1000.0);
    }
}
