package com.trabalho.finalpc.consumer.messaging; // Adaptadores de mensageria do consumidor

import com.trabalho.finalpc.common.ProductMessage;              // DTO que retornamos
import com.trabalho.finalpc.common.ProductType;                 // Tipo solicitado nesta iteração
import com.trabalho.finalpc.common.messaging.MessageSubscriber; // Porta que implementamos (simulada)
import org.slf4j.Logger;                                        // Logger de acompanhamento
import org.slf4j.LoggerFactory;

/**
 * Este adaptador NÃO busca no broker. Eu simulo o recebimento criando um ProductMessage.
 * É útil para testar o ConsumerService sem depender da infraestrutura do Rabbit.
 */
public class NoOpSubscriber implements MessageSubscriber {
    private static final Logger log = LoggerFactory.getLogger(NoOpSubscriber.class);

    @Override
    public ProductMessage receiveOne(ProductType type) { // Simulamos “1 mensagem” do tipo solicitado
        ProductMessage msg = new ProductMessage();       // Gera itemId e timestamp automaticamente
        msg.setType(type);                               // Define o tipo conforme necessidade
        msg.setProducerId("no-broker");                 // Indica que veio da simulação
        log.info("[NoOpSubscriber] Simulando recebimento => itemId={} tipo={}", msg.getItemId(), msg.getType());
        return msg;                                      // Entregamos a “mensagem” ao serviço
    }

    @Override
    public void close() { // Nenhum recurso externo
    }
}
