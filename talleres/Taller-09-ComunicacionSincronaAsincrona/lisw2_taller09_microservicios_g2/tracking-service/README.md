# tracking-service

Calcula las estadísticas de los usuarios a partir de los simulacros resueltos.

| Dato | Valor |
|---|---|
| Contexto delimitado | Seguimiento Académico |
| Puerto | `8089` |
| Paquete | `co.edu.unicauca.tracking` |
| Persistencia | H2 en memoria propia (`tracking_db`) |
| Variables | `SERVER_PORT`, `RABBITMQ_HOST`, `RABBITMQ_PORT`, `RABBITMQ_USER`, `RABBITMQ_PASSWORD`, `DIRECT_EXCHANGE`, `TOPIC_EXCHANGE` |

## Endpoints (Spring Web)

- `GET /tracking/users/{id}`
- `GET /tracking/summary`

## Mensajes publicados (`RabbitTemplate`)

- Ninguno

## Mensajes consumidos (`@RabbitListener`)

- `SimulationCompleted`

## Ejecutar

```bash
mvn spring-boot:run
```

Necesita RabbitMQ en `localhost:5672` (ver el README de la raíz).
