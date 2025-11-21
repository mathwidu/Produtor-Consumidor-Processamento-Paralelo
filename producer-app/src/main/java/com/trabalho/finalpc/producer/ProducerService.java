package com.trabalho.finalpc.producer; // Pacote da aplicação produtora

import com.trabalho.finalpc.common.ProductMessage;              // DTO da mensagem a ser publicada
import com.trabalho.finalpc.common.ProductType;                 // Tipo do produto (A/B)
import com.trabalho.finalpc.common.Timing;                      // Tempos de produção/consumo
import com.trabalho.finalpc.common.messaging.MessagePublisher;  // Porta/contrato de publicação (broker)
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.time.Instant;                                       // Para exibir timestamp ISO nos logs
import java.util.Locale;                                        // Para formatar segundos como 3.5 s

/**
 * Este serviço isola a regra de negócio do produtor:
 * 1) Eu escolho o tipo (estratégia)
 * 2) Eu aplico o tempo de produção (sleep conforme Timing)
 * 3) Eu construo a mensagem (ProductMessage)
 * 4) Eu publico via porta MessagePublisher (Rabbit/NoOp)
 * Observação: esta separação me permite testar o loop com NoOp e trocar para Rabbit sem tocar nas regras.
 */
public class ProducerService { // Classe de serviço (stateless)

    private static final Logger log = LoggerFactory.getLogger(ProducerService.class);

    private final String producerId;        // Quem somos (para logs e auditoria)
    private final MessagePublisher publisher; // Adaptador de publicação (Rabbit/NoOp)
    private final TypeSelectionStrategy typeStrategy; // Política de escolha do tipo (aleatória 50/50)

    /** Construtor com injeção de dependências (contratos). */
    public ProducerService(String producerId,
                           MessagePublisher publisher,
                           TypeSelectionStrategy typeStrategy) {
        this.producerId = producerId;       // Armazena o ID do produtor
        this.publisher = publisher;         // Armazena o publicador (broker)
        this.typeStrategy = typeStrategy;   // Armazena a estratégia de seleção de tipos
    }

    /**
     * Loop principal de produção (executado na thread principal ou dedicada).
     * Implementação mínima (sem mensageria real): usa MessagePublisher fornecido (pode ser NoOp).
     * Fluxo por iteração:
     * 1) Obter próximo tipo via estratégia
     * 2) Dormir pelo tempo de produção do tipo
     * 3) Criar ProductMessage com campos preenchidos
     * 4) Publicar via MessagePublisher
     * 5) Logar informações
     */
    public void runLoop(int iterations) { // Executa N iterações (configurável via ENV para o vídeo)
        for (int i = 1; i <= iterations; i++) {
            ProductType type = typeStrategy.nextType(); // 1) Decide A/B nesta rodada
            long prodMs = Timing.productionMillis(type); // 2) Calcula tempo de produção conforme tipo
            try {
                log.info("[Producer {}] Iteração {} => tipo={}, produzindo por {} ({} ms)",
                        producerId, i, type, fmtSeconds(prodMs), prodMs);
                Thread.sleep(prodMs); // 2) Simula o trabalho de produzir
                ProductMessage msg = buildMessage(type); // 3) Cria mensagem com ID/ts/type/producerId
                publisher.publish(type, msg); // 4) Publica pela porta (Rabbit/NoOp)
                Instant tsIso = Instant.ofEpochMilli(msg.getTimestamp()); // Converte ts para ISO para legibilidade
                log.info("[Producer {}] Publicado itemId={} tipo={} ts={} ({} ISO)",
                        producerId, msg.getItemId(), msg.getType(), msg.getTimestamp(), tsIso);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt(); // Respeitamos a interrupção (boa prática em Java)
                log.warn("[Producer {}] Interrompido durante produção.", producerId);
                break;
            } catch (Exception e) {
                log.error("[Producer {}] Falha ao publicar: {}", producerId, e.getMessage(), e);
            }
        }
    }

    /**
     * Constrói a mensagem para publicação. Isolado para facilitar testes.
     */
    ProductMessage buildMessage(ProductType type) { // Método isolado para facilitar teste unitário
        ProductMessage msg = new ProductMessage();  // itemId e timestamp gerados no construtor
        msg.setType(type);                          // definimos o tipo (A/B)
        msg.setProducerId(producerId);              // gravamos quem produziu (útil nos logs e UI)
        return msg;                                 // mensagem pronta para (de)serialização JSON
    }

    // Formata milissegundos em segundos com uma casa (ex.: 3.5 s)
    private static String fmtSeconds(long ms) { // Helper: 3500 ms -> "3.5 s"
        return String.format(Locale.US, "%.1f s", ms / 1000.0);
    }
}
