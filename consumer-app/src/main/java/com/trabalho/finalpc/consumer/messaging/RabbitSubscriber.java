package com.trabalho.finalpc.consumer.messaging; // Adaptador de consumo real (RabbitMQ)

import com.rabbitmq.client.Channel;             // Canal AMQP (operações de get/declaração)
import com.rabbitmq.client.Connection;          // Conexão AMQP
import com.rabbitmq.client.ConnectionFactory;   // Fábrica de conexão
import com.rabbitmq.client.GetResponse;         // Resposta do basicGet
import com.trabalho.finalpc.common.MessagingConstants; // Nomes de exchange/filas/rk
import com.trabalho.finalpc.common.ProductMessage;
import com.trabalho.finalpc.common.ProductType;
import com.trabalho.finalpc.common.config.AppConfig;    // Host/porta/user/pass do broker
import com.trabalho.finalpc.common.messaging.MessageSubscriber;               // Porta que implementamos
import com.trabalho.finalpc.common.serialization.JacksonProductMessageCodec;  // JSON com Jackson
import com.trabalho.finalpc.common.serialization.JsonCodec;                   // Abstração de JSON
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets; // Decodificação do corpo

/**
 * Este adaptador consome 1 mensagem do tipo desejado usando basicGet (pull).
 * Minha decisão: autoAck=true para simplicidade (a mensagem sai da fila ao receber). Para robustez extra,
 * eu poderia usar autoAck=false e chamar basicAck após o “sleep”, mas não é exigência do enunciado.
 */
public class RabbitSubscriber implements MessageSubscriber {
    private static final Logger log = LoggerFactory.getLogger(RabbitSubscriber.class);

    private final Connection connection;
    private final Channel channel;
    private final JsonCodec<ProductMessage> codec;

    public RabbitSubscriber() throws Exception {
        this(new JacksonProductMessageCodec());
    }

    public RabbitSubscriber(JsonCodec<ProductMessage> codec) throws Exception { // Permite testar com outro codec
        this.codec = codec;

        ConnectionFactory factory = new ConnectionFactory();           // Config de conexão AMQP
        factory.setHost(AppConfig.getRabbitHost());
        factory.setPort(AppConfig.getRabbitPort());
        factory.setUsername(AppConfig.getRabbitUser());
        factory.setPassword(AppConfig.getRabbitPass());

        this.connection = factory.newConnection("consumer-app");      // Abre conexão
        this.channel = connection.createChannel();                     // Cria canal

        // Garante topologia (idempotente)
        channel.exchangeDeclare(MessagingConstants.EXCHANGE_PRODUCTS, "direct", true); // exchange durável
        channel.queueDeclare(MessagingConstants.QUEUE_TYPE_A, true, false, false, null); // filas duráveis
        channel.queueDeclare(MessagingConstants.QUEUE_TYPE_B, true, false, false, null);
        channel.queueBind(MessagingConstants.QUEUE_TYPE_A, MessagingConstants.EXCHANGE_PRODUCTS, MessagingConstants.ROUTING_KEY_TYPE_A);
        channel.queueBind(MessagingConstants.QUEUE_TYPE_B, MessagingConstants.EXCHANGE_PRODUCTS, MessagingConstants.ROUTING_KEY_TYPE_B);

        log.info("[RabbitSubscriber] Conectado a {}:{} como {}. Filas prontas.",
                AppConfig.getRabbitHost(), AppConfig.getRabbitPort(), AppConfig.getRabbitUser());
    }

    @Override
    public ProductMessage receiveOne(ProductType type) throws Exception {
        final String queue = (type == ProductType.TIPO_A) // Tipo solicitado -> fila correspondente
                ? MessagingConstants.QUEUE_TYPE_A
                : MessagingConstants.QUEUE_TYPE_B;

        // Polling até encontrar uma mensagem desse tipo
        while (true) {
            GetResponse resp = channel.basicGet(queue, true); // autoAck=true (remove ao receber)
            if (resp != null) {
                String json = new String(resp.getBody(), StandardCharsets.UTF_8); // Decodifica corpo
                ProductMessage msg = codec.fromJson(json);                         // JSON -> DTO
                log.info("[RabbitSubscriber] Recebido de queue='{}' => {}", queue, json); // Log útil na demo
                return msg; // Entregamos a mensagem ao serviço para “consumo” (sleep)
            }
            Thread.sleep(300); // Backoff pequeno para evitar busy-wait quando a fila está vazia
        }
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
