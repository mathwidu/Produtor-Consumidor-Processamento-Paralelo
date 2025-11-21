#!/usr/bin/env bash
set -euo pipefail

docker compose up -d
echo "RabbitMQ em execução. UI: http://localhost:15672 (guest/guest)"

