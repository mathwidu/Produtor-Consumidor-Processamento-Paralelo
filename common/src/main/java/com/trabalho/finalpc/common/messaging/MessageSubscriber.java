package com.trabalho.finalpc.common.messaging; // Pacote para contratos de mensageria

import com.trabalho.finalpc.common.ProductMessage; // DTO da mensagem recebida do broker
import com.trabalho.finalpc.common.ProductType;    // Tipo de produto (mapeia para fila específica)

/**
 * Esta é a “porta” de entrada de mensagens vindas do broker que eu defini.
 * Decisão importante: para aderir ao enunciado (escolher tipo a cada iteração), eu adotei consumo pull
 * com basicGet, pois preciso pegar “exatamente 1 mensagem do tipo escolhido”.
 */
public interface MessageSubscriber extends AutoCloseable {
    /**
     * Obtém exatamente uma mensagem do tipo informado (bloqueante ou com polling).
     * Implementação deve tratar ack/nack de forma explícita.
     */
    ProductMessage receiveOne(ProductType type) throws Exception;

    /** Fecha recursos (conexões/canais) ao encerrar. */
    @Override
    void close() throws Exception;
}
