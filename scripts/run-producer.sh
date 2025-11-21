#!/usr/bin/env bash
set -euo pipefail

# Uso: bash scripts/run-producer.sh <PRODUCER_ID> <ITERATIONS>
PID=${1:-producer-1}
ITERS=${2:-10}

export USE_RABBIT=${USE_RABBIT:-true}
export PRODUCER_ID="$PID"
export ITERATIONS="$ITERS"

JAR=$(ls -1 producer-app/target/producer-app.jar 2>/dev/null || true)
if [[ -z "$JAR" ]]; then
  echo "Fat-jar não encontrado. Rodando build..."
  mvn -q -DskipTests package
  JAR=$(ls -1 producer-app/target/producer-app.jar)
fi

echo "Iniciando produtor $PRODUCER_ID com ITERATIONS=$ITERATIONS (USE_RABBIT=$USE_RABBIT)"
java -jar "$JAR"

