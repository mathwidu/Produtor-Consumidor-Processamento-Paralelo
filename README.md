# Trabalho Final – Produtor–Consumidor com Mensageria (RabbitMQ)

Este repositório implementa e demonstra o problema Produtor–Consumidor com 2 produtores e 4 consumidores, 2 tipos de produto (A/B) e tempos distintos de produção/consumo, comunicando via RabbitMQ. Foi desenvolvido em Java 17 (Maven multi‑módulo).

- Produtores: publicam mensagens (ProductMessage) no exchange `products` com routing keys `typeA`/`typeB`.
- Consumidores: escolhem aleatoriamente o tipo necessário a cada iteração e consomem 1 mensagem da fila correspondente (`products.typeA`/`products.typeB`).
- Tempos: produção A=3.5s, B=7.5s; consumo é o dobro: A=7.0s, B=15.0s.

## Sumário
- Requisitos do trabalho e como atendemos
- Arquitetura e decisões
- Pré‑requisitos
- Como executar (Maven e fat‑jar)
- Variáveis de ambiente
- Demonstração 2P + 4C
- Observabilidade no RabbitMQ
- Possíveis melhorias

## Requisitos do trabalho e como atendemos
1) Dois (02) produtores que geram itens aleatoriamente de dois tipos (A/B)
   - Implementado: `producer-app` com `RandomTypeSelectionStrategy` + `ProducerService`.
2) Quatro (04) consumidores que precisam aleatoriamente de um dos dois tipos
   - Implementado: `consumer-app` com `RandomNeedSelectionStrategy` + `ConsumerService`.
3) Tempos distintos por tipo; consumo é o dobro
   - Implementado em `common/Timing`: A=3.5s/7.0s; B=7.5s/15.0s.
4) Serviços independentes (processos separados) em Java
   - Rodamos 2 instâncias de `ProducerApp` e 4 de `ConsumerApp` com IDs distintos.
5) Comunicação via mensageria
   - RabbitMQ (`docker-compose.yml`) com exchange direct `products`, filas `products.typeA`/`products.typeB` e routing keys `typeA`/`typeB`.

## Arquitetura e decisões
- Topologia RabbitMQ (AMQP):
  - Exchange `products` (direct) 
  - Bindings: `typeA` → `products.typeA`; `typeB` → `products.typeB`.
  - Mensagens persistentes (deliveryMode=2) e filas/ exchange duráveis.
- Produtor:
  - Seleciona A/B aleatoriamente → simula produção (sleep) → publica JSON.
  - `ProducerService` fala com a “porta” `MessagePublisher`; adaptadores: `RabbitPublisher` e `NoOpPublisher`.
- Consumidor:
  - Seleciona necessidade A/B → faz pull `basicGet` na fila do tipo → simula consumo (dobro do tempo).
  - `ConsumerService` fala com a “porta” `MessageSubscriber`; adaptadores: `RabbitSubscriber` e `NoOpSubscriber`.
- Por que pull (basicGet)?
  - O enunciado pede decidir o tipo necessário a cada iteração; `basicGet` nos permite “pegar exatamente 1 do tipo escolhido”.
- Ack (autoAck=true):
  - Simples para o escopo; remove a mensagem ao receber. Para robustez extra poderíamos usar ack manual (autoAck=false + basicAck após o processamento).
- Config por ENV (`AppConfig`):
  - IDs dos serviços e dados do broker (host/porta/user/pass) com defaults sensatos.

## Pré‑requisitos
- Java 17+
- Maven 3.9+
- Docker + Docker Compose (para o RabbitMQ)

## Como executar
### 1) Subir o RabbitMQ
- `docker compose up -d`
- UI: http://localhost:15672 (user: guest, pass: guest)

### 2) Opção A — Rodar com Maven (simples)
- Produtor (exemplo):
  - `PRODUCER_ID=producer-1 ITERATIONS=5 USE_RABBIT=true mvn -f producer-app/pom.xml -Dexec.mainClass=com.trabalho.finalpc.producer.ProducerApp exec:java`
- Consumidor (exemplo):
  - `CONSUMER_ID=consumer-1 ITERATIONS=5 USE_RABBIT=true mvn -f consumer-app/pom.xml -Dexec.mainClass=com.trabalho.finalpc.consumer.ConsumerApp exec:java`

### 3) Opção B — Rodar com fat‑jar (java -jar)
- Construir:
  - `mvn -q -DskipTests package`
- Executar:
  - Produtor: `USE_RABBIT=true PRODUCER_ID=producer-1 ITERATIONS=5 java -jar producer-app/target/producer-app.jar`
  - Consumidor: `USE_RABBIT=true CONSUMER_ID=consumer-1 ITERATIONS=5 java -jar consumer-app/target/consumer-app.jar`

## Variáveis de ambiente
- USE_RABBIT=true|false — usa RabbitMQ (true) ou adaptador NoOp (false)
- RABBITMQ_HOST, RABBITMQ_PORT, RABBITMQ_USER, RABBITMQ_PASS — config do broker (defaults: localhost, 5672, guest/guest)
- PRODUCER_ID / CONSUMER_ID — identifica o processo (ex.: producer-1)
- ITERATIONS — número de iterações antes de encerrar o processo (ex.: 10)

Arquivo de exemplo: `.env.example` 

## Demonstração 2P + 4C (atalhos)
- Subir Rabbit e gerar JARs:
  - `bash scripts/up-rabbit.sh && bash scripts/build.sh`
- Em terminais separados, execute:
  - 2 produtores:
    - `USE_RABBIT=true bash scripts/run-producer.sh producer-1 20`
    - `USE_RABBIT=true bash scripts/run-producer.sh producer-2 20`
  - 4 consumidores:
    - `USE_RABBIT=true bash scripts/run-consumer.sh consumer-1 10`
    - `USE_RABBIT=true bash scripts/run-consumer.sh consumer-2 10`
    - `USE_RABBIT=true bash scripts/run-consumer.sh consumer-3 10`
    - `USE_RABBIT=true bash scripts/run-consumer.sh consumer-4 10`
- (Opcional) Em um terminal só: `bash scripts/run-demo.sh` (inicia tudo em background)

## Observabilidade no RabbitMQ
- UI: http://localhost:15672
- Exchange `products` (direct) com bindings `typeA` e `typeB`.
- Filas `products.typeA`/`products.typeB` com Ready (mensagens pendentes) e Consumers (processos conectados).

## Possíveis melhorias (bônus)
- Ack manual (autoAck=false + basicAck após o processamento) para maior resiliência.
- Modo push (basicConsume) com prefetch=1 caso a necessidade por tipo não varie a cada iteração.
- Kubernetes/Helm para orquestrar 2P+4C em cluster.

---

Para um relato mais detalhado das escolhas e do fluxo (nível acadêmico), consulte `PLANO_DO_TRABALHO.md`, que contém a visão, o mapeamento de requisitos, e um roteiro de apresentação.

