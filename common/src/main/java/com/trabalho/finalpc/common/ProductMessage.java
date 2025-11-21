package com.trabalho.finalpc.common; // Pacote compartilhado entre produtores/consumidores

import java.time.Instant; // Usado para obter o timestamp atual de criação
import java.util.UUID;    // Usado para gerar IDs únicos (identidade do item)

/**
 * Este DTO é o “contrato” do payload que trafega no RabbitMQ.
 * Minhas decisões:
 * - Eu uso um UUID no itemId para cada unidade produzida (rastreabilidade fim a fim).
 * - Eu guardo o type (A/B) para roteamento e para que o consumidor identifique o tipo recebido.
 * - Eu mantenho o producerId para diferenciar qual processo gerou o item (útil na demonstração com 2 produtores).
 * - O timestamp guarda o momento de criação (epoch ms) e serve para auditar tempos e ordem aproximada.
 */
public class ProductMessage { // Declaração da classe DTO (Data Transfer Object)
    private String itemId;       // ID único do item (gerado automaticamente no construtor)
    private ProductType type;    // Tipo do produto (TIPO_A/TIPO_B)
    private String producerId;   // Identificador lógico do produtor (ex.: producer-1)
    private long timestamp;      // Momento de criação da mensagem (epoch ms)

    public ProductMessage() {    // Construtor padrão (gera itemId e timestamp automaticamente)
        this.itemId = UUID.randomUUID().toString();          // Eu uso UUID para unicidade global e fácil log
        this.timestamp = Instant.now().toEpochMilli();       // Eu salvo o ts de criação para auditoria/observabilidade
    }

    // Getters e setters canônicos (mantemos encapsulamento e facilitamos (de)serialização JSON)
    public String getItemId() { return itemId; }
    public void setItemId(String itemId) { this.itemId = itemId; }

    public ProductType getType() { return type; }
    public void setType(ProductType type) { this.type = type; }

    public String getProducerId() { return producerId; }
    public void setProducerId(String producerId) { this.producerId = producerId; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}
