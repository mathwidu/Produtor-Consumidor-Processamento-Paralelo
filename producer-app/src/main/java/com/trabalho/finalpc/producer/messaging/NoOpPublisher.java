package com.trabalho.finalpc.producer.messaging; // Pacote com adaptadores de mensageria do produtor

import com.trabalho.finalpc.common.ProductMessage;             // DTO a publicar
import com.trabalho.finalpc.common.ProductType;                // Tipo -> apenas logamos aqui
import com.trabalho.finalpc.common.messaging.MessagePublisher; // Porta que implementamos (NoOp)
import org.slf4j.Logger;                                       // Logs para acompanhamento
import org.slf4j.LoggerFactory;

/**
 * Este adaptador NÃO usa broker (No-Op). Eu o uso para testes rápidos e vídeo pedagógico.
 * Eu mantenho um caminho alternativo que não depende de RabbitMQ para validar a lógica do produtor.
 */
public class NoOpPublisher implements MessagePublisher {
    private static final Logger log = LoggerFactory.getLogger(NoOpPublisher.class);

    @Override
    public void publish(ProductType type, ProductMessage message) { // Apenas loga a “publicação”
        log.info("[NoOpPublisher] Simulando publicação => itemId={} tipo={} producerId={}",
                message.getItemId(), type, message.getProducerId()); // Nada é enviado de fato
    }

    @Override
    public void close() { // Não há recursos a fechar (diferente do Rabbit)
    }
}
