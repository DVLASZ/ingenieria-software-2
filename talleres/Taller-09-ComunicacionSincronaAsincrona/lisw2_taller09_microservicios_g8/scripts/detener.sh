#!/usr/bin/env bash
# Detiene los servicios que levantó levantar.sh y, con --rabbit, también el contenedor de RabbitMQ.
RAIZ="$(cd "$(dirname "$0")/.." && pwd)"
cd "$RAIZ"

if [ -f logs/pids ]; then
  while IFS=: read -r nombre pid; do
    [ -z "$pid" ] && continue
    if kill "$pid" 2>/dev/null; then echo "  $nombre detenido"; fi
  done < logs/pids
  rm -f logs/pids
fi

if [ "$1" = "--rabbit" ]; then
  docker compose down
fi
