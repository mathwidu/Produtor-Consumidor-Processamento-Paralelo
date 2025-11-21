package com.trabalho.finalpc.common.messaging; // Pacote para contratos de mensageria

import com.trabalho.finalpc.common.ProductMessage; // DTO da mensagem (payload)
import com.trabalho.finalpc.common.ProductType;    // Tipo do produto (define routing key)

/**
 * Esta é a “porta” de saída do domínio para a infraestrutura (mensageria) que eu defini.
 * Minhas decisões:
 * - Programar contra interface para poder trocar adaptadores (NoOp ↔ RabbitMQ) sem afetar regra de negócio.
 * - O tipo do produto determina a routing key (mapeado no adaptador concreto).
 */
public interface MessagePublisher {
    /**
     * Publica uma mensagem para o tipo informado. O adaptador mapeia type -> routing key.
     * Observação: em cenários reais, poderíamos reforçar idempotência (chaves de deduplicação).
     */
    void publish(ProductType type, ProductMessage message) throws Exception;

    /** Fecha recursos (conexões/canais) quando a aplicação encerrar. */
    void close() throws Exception;
}
