#!/usr/bin/env bash
set -euo pipefail

# Compila os módulos e gera os fat-jars executáveis
mvn -q -DskipTests package
echo "JARs gerados em:"
ls -1 producer-app/target/producer-app.jar consumer-app/target/consumer-app.jar 2>/dev/null || true

