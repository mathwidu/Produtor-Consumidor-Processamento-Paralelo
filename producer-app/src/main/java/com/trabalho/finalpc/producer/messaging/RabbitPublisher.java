package com.trabalho.finalpc.producer.messaging; // Adaptador de publicação real (RabbitMQ)

import com.rabbitmq.client.AMQP;                // Propriedades de mensagem (contentType, deliveryMode, timestamp)
import com.rabbitmq.client.Channel;             // Canal AMQP (operações de publicação/declaração)
import com.rabbitmq.client.Connection;          // Conexão AMQP
import com.rabbitmq.client.ConnectionFactory;   // Fábrica de conexões
import com.trabalho.finalpc.common.MessagingConstants; // Nomes padronizados (exchange/filas/rk)
import com.trabalho.finalpc.common.ProductMessage;
import com.trabalho.finalpc.common.ProductType;
import com.trabalho.finalpc.common.config.AppConfig;    // Lê host/porta/user/pass do broker
import com.trabalho.finalpc.common.messaging.MessagePublisher;
import com.trabalho.finalpc.common.serialization.JsonCodec;               // Abstração de JSON
import com.trabalho.finalpc.common.serialization.JacksonProductMessageCodec; // Implementação Jackson
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets; // Encoding para o corpo da mensagem
import java.util.Date;                    // Timestamp AMQP (compatível com UI do Rabbit)

/**
 * Este adaptador publica mensagens reais no RabbitMQ.
 * Minhas decisões:
 * - Declarar exchange/filas/bindings na construção (idempotente) para simplificar setup.
 * - Publicar mensagens persistentes (deliveryMode=2) em JSON com contentType explícito.
 */
public class RabbitPublisher implements MessagePublisher {
    private static final Logger log = LoggerFactory.getLogger(RabbitPublisher.class);

    private final Connection connection;
    private final Channel channel;
    private final JsonCodec<ProductMessage> codec;

    public RabbitPublisher() throws Exception {
        this(new JacksonProductMessageCodec());
    }

    public RabbitPublisher(JsonCodec<ProductMessage> codec) throws Exception { // Permite injetar outro codec para testes
        this.codec = codec;

        ConnectionFactory factory = new ConnectionFactory();           // Configura fábrica de conexão AMQP
        factory.setHost(AppConfig.getRabbitHost());                    // Host (default: localhost)
        factory.setPort(AppConfig.getRabbitPort());                    // Porta (default: 5672)
        factory.setUsername(AppConfig.getRabbitUser());                // Usuário (default: guest)
        factory.setPassword(AppConfig.getRabbitPass());                // Senha (default: guest)

        this.connection = factory.newConnection("producer-app");      // Abre conexão com nome amigável
        this.channel = connection.createChannel();                     // Cria canal para publicar e declarar topologia

        // Declara topologia (idempotente)
        channel.exchangeDeclare(MessagingConstants.EXCHANGE_PRODUCTS, "direct", true); // exchange durável
        channel.queueDeclare(MessagingConstants.QUEUE_TYPE_A, true, false, false, null); // filas duráveis
        channel.queueDeclare(MessagingConstants.QUEUE_TYPE_B, true, false, false, null);
        channel.queueBind(MessagingConstants.QUEUE_TYPE_A, MessagingConstants.EXCHANGE_PRODUCTS, MessagingConstants.ROUTING_KEY_TYPE_A);
        channel.queueBind(MessagingConstants.QUEUE_TYPE_B, MessagingConstants.EXCHANGE_PRODUCTS, MessagingConstants.ROUTING_KEY_TYPE_B);

        log.info("[RabbitPublisher] Conectado a {}:{} como {}. Exchange '{}' pronto.",
                AppConfig.getRabbitHost(), AppConfig.getRabbitPort(), AppConfig.getRabbitUser(), MessagingConstants.EXCHANGE_PRODUCTS);
    }

    @Override
    public void publish(ProductType type, ProductMessage message) throws Exception {
        String routingKey = (type == ProductType.TIPO_A)               // Mapeia tipo -> routing key
                ? MessagingConstants.ROUTING_KEY_TYPE_A
                : MessagingConstants.ROUTING_KEY_TYPE_B;

        String json = codec.toJson(message);                           // Serializa DTO -> JSON
        byte[] body = json.getBytes(StandardCharsets.UTF_8);           // Codifica como UTF-8

        AMQP.BasicProperties props = new AMQP.BasicProperties.Builder() // Define metadados úteis
                .contentType("application/json")                       // Ajuda a UI e consumidores a entenderem o payload
                .deliveryMode(2)                                       // 2 = persistente (sobrevive a restart do broker)
                .timestamp(new Date(message.getTimestamp()))           // Timestamp alinhado ao DTO
                .build();

        channel.basicPublish(MessagingConstants.EXCHANGE_PRODUCTS, routingKey, props, body); // Publica de fato
        log.info("[RabbitPublisher] Publicado em rk='{}' => {}", routingKey, json);          // Log para auditoria
    }

    @Override
    public void close() throws Exception { // Fecha canal e conexão com segurança
        try {
            if (channel != null && channel.isOpen()) channel.close();
        } finally {
            if (connection != null && connection.isOpen()) connection.close();
        }
    }
}
