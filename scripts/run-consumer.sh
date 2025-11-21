#!/usr/bin/env bash
set -euo pipefail

# Uso: bash scripts/run-consumer.sh <CONSUMER_ID> <ITERATIONS>
CID=${1:-consumer-1}
ITERS=${2:-10}

export USE_RABBIT=${USE_RABBIT:-true}
export CONSUMER_ID="$CID"
export ITERATIONS="$ITERS"

JAR=$(ls -1 consumer-app/target/consumer-app.jar 2>/dev/null || true)
if [[ -z "$JAR" ]]; then
  echo "Fat-jar não encontrado. Rodando build..."
  mvn -q -DskipTests package
  JAR=$(ls -1 consumer-app/target/consumer-app.jar)
fi

echo "Iniciando consumidor $CONSUMER_ID com ITERATIONS=$ITERATIONS (USE_RABBIT=$USE_RABBIT)"
java -jar "$JAR"

