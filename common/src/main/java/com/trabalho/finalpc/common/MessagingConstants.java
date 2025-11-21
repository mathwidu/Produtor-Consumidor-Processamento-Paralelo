package com.trabalho.finalpc.common; // Pacote comum (contratos compartilhados)

/**
 * Eu centralizo aqui os nomes da topologia AMQP para evitar divergências.
 * Minhas decisões:
 * - Um exchange do tipo direct chamado "products" (roteamento por routing key exata).
 * - Duas filas: uma para A e outra para B, facilitando paralelismo e separação de cargas.
 * - Duas routing keys: "typeA" e "typeB" (nomenclatura simples e explícita).
 */
public final class MessagingConstants { // Classe utilitária final (não instanciável)
    // Exchange principal: onde produtores publicam e de onde consumidores recebem via bindings
    public static final String EXCHANGE_PRODUCTS = "products";

    // Filas por tipo de produto (decisão: 1 fila por tipo facilita métricas e escalabilidade por tipo)
    public static final String QUEUE_TYPE_A = "products.typeA"; // Fila para itens do tipo A
    public static final String QUEUE_TYPE_B = "products.typeB"; // Fila para itens do tipo B

    // Routing keys usadas na publicação (publish) e nos bindings (exchange -> queue)
    public static final String ROUTING_KEY_TYPE_A = "typeA"; // Mensagens de A roteiam para QUEUE_TYPE_A
    public static final String ROUTING_KEY_TYPE_B = "typeB"; // Mensagens de B roteiam para QUEUE_TYPE_B

    private MessagingConstants() { /* Impede instanciação */ }
}
