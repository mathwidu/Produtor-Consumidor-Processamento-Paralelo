#!/usr/bin/env bash
set -euo pipefail

# Sobe RabbitMQ e executa 2 produtores + 4 consumidores (logs no console).

bash scripts/up-rabbit.sh
bash scripts/build.sh

# Exemplo: ajustar ITERATIONS via ENV antes de chamar este script, se quiser.

echo "=== Iniciando produtores ==="
USE_RABBIT=true bash scripts/run-producer.sh producer-1 20 &
USE_RABBIT=true bash scripts/run-producer.sh producer-2 20 &

echo "=== Iniciando consumidores ==="
USE_RABBIT=true bash scripts/run-consumer.sh consumer-1 10 &
USE_RABBIT=true bash scripts/run-consumer.sh consumer-2 10 &
USE_RABBIT=true bash scripts/run-consumer.sh consumer-3 10 &
USE_RABBIT=true bash scripts/run-consumer.sh consumer-4 10 &

echo "Processos iniciados em background. Use 'jobs -l' para ver PIDs."
echo "UI do RabbitMQ: http://localhost:15672 (guest/guest)"

