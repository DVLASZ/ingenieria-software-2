#!/usr/bin/env bash
# Compila (si hace falta) y levanta los nueve servicios con `java -jar`.
#   ./scripts/levantar.sh              levanta RabbitMQ con Docker (si no responde) y los servicios
#   ./scripts/levantar.sh --sin-docker usa el RabbitMQ que ya esté en localhost:5672 (instalado en el equipo)
# Los registros quedan en logs/<servicio>.log y los identificadores de proceso en logs/pids.
set -e
RAIZ="$(cd "$(dirname "$0")/.." && pwd)"
cd "$RAIZ"
mkdir -p logs

JAVA="java"
[ -n "$JAVA_HOME" ] && JAVA="$JAVA_HOME/bin/java"

SERVICIOS="user-service:8081 validation-service:8082 lifecycle-service:8084 question-service:8083 review-service:8085 notification-service:8086 history-service:8087 simulation-service:8088 tracking-service:8089"

rabbit_arriba() { (echo > /dev/tcp/localhost/5672) >/dev/null 2>&1; }

if ! rabbit_arriba; then
  if [ "$1" = "--sin-docker" ]; then
    echo "No hay un RabbitMQ en localhost:5672. Inícialo y vuelve a ejecutar."; exit 1
  fi
  echo "Levantando RabbitMQ con Docker..."
  docker compose up -d
  # el puerto se abre antes de que RabbitMQ esté listo: se espera a que Docker lo marque como saludable
  for i in $(seq 1 60); do
    [ "$(docker inspect -f '{{.State.Health.Status}}' saberpro-rabbitmq 2>/dev/null)" = "healthy" ] && break
    sleep 2
  done
  [ "$(docker inspect -f '{{.State.Health.Status}}' saberpro-rabbitmq 2>/dev/null)" = "healthy" ] || { echo "RabbitMQ no quedó saludable"; exit 1; }
fi
echo "RabbitMQ listo (consola: http://localhost:15672, guest / guest)"

if ! ls */target/*-SNAPSHOT.jar >/dev/null 2>&1; then
  echo "Compilando..."
  mvn -q -DskipTests package
fi

: > logs/pids
for par in $SERVICIOS; do
  nombre="${par%%:*}"; puerto="${par##*:}"
  jar=$(ls "$nombre"/target/*-SNAPSHOT.jar | head -1)
  SERVER_PORT="$puerto" nohup "$JAVA" -jar "$jar" > "logs/$nombre.log" 2>&1 &
  echo "$nombre:$!" >> logs/pids
  echo "  $nombre arrancando en el puerto $puerto"
done

echo "Esperando a que respondan..."
for par in $SERVICIOS; do
  nombre="${par%%:*}"; puerto="${par##*:}"
  for i in $(seq 1 60); do
    codigo=$(curl -s -o /dev/null -w '%{http_code}' "http://localhost:$puerto/" || true)
    [ "$codigo" != "000" ] && break
    sleep 2
  done
  [ "$codigo" = "000" ] && echo "  $nombre NO respondió (revisa logs/$nombre.log)" || echo "  $nombre listo"
done
echo "Todo en marcha. Para detenerlo: ./scripts/detener.sh"
