# Banco de preguntas Saber Pro: DDD, hexagonal y Docker

Taller 13 del Laboratorio de Ingeniería de Software II (grupo 2). Son tres microservicios Spring Boot y un RabbitMQ que se levantan juntos con
Docker Compose en una red propia (`saberpro-net`).

| Servicio | Puerto | Qué hace |
|---|---|---|
| validation-service | 8082 | `POST /validate`: dice si una justificación tiene al menos 15 caracteres |
| question-service | 8083 | El banco de preguntas, con dominio puro y arquitectura hexagonal. Guarda con JPA en H2 o en memoria |
| lifecycle-service | 8084 | Recibe por RabbitMQ la petición de revisión y pasa la pregunta de BORRADOR a PENDIENTE_REVISION |
| RabbitMQ | 5672 y 15672 | Panel en http://localhost:15672, usuario `guest` y clave `guest` |

## Cómo correrlo

Hace falta Docker con Compose. No hace falta tener Java ni Maven: los jar se compilan dentro de las imágenes.

```bash
docker compose up -d --build
docker ps
docker logs -f saberpro-question-service
```

Para apagarlo: `docker compose down`.

## Cómo probarlo

Se importa `postman/saberpro-taller13.postman_collection.json` y se corren las 11 peticiones en orden. Cada una trae su prueba. Con curl
sería así:

```bash
curl -i http://localhost:8083/api/preguntas
curl -i -X POST http://localhost:8083/api/preguntas -H "Content-Type: application/json" -d @pregunta.json
curl -i -X POST http://localhost:8083/api/preguntas/{id}/enviar-revision
```

Crear devuelve 201, o 400 si la justificación es corta. Enviar a revisión devuelve 202 enseguida y unos segundos después la pregunta queda en
PENDIENTE_REVISION. En el panel de RabbitMQ se ve la cola `pregunta.revisar.queue` y en los logs de `saberpro-lifecycle-service` el cambio de
estado.

## Cómo cambiar el adaptador de persistencia

question-service guarda con JPA y H2 por defecto. Para usar el adaptador en memoria se levanta así y el dominio no cambia:

```bash
SABERPRO_PERSISTENCIA=memoria docker compose up -d question-service
```

En PowerShell: `$env:SABERPRO_PERSISTENCIA="memoria"; docker compose up -d question-service`. Para volver a JPA se quita la variable y se
repite el comando.

## Pruebas

Cada servicio se prueba con `mvn test` dentro de su carpeta (Java 17 o superior). Son 100 pruebas en total.
