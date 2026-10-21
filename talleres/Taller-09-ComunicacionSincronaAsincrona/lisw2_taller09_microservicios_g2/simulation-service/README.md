# simulation-service

Mantiene un catálogo de preguntas publicadas y permite resolver simulacros.

| Dato | Valor |
|---|---|
| Contexto delimitado | Simulacros de Evaluación |
| Puerto | `8088` |
| Paquete | `co.edu.unicauca.simulation` |
| Persistencia | H2 en memoria propia (`simulation_db`) |
| Variables | `SERVER_PORT`, `RABBITMQ_HOST`, `RABBITMQ_PORT`, `RABBITMQ_USER`, `RABBITMQ_PASSWORD`, `DIRECT_EXCHANGE`, `TOPIC_EXCHANGE`, `QUESTION_SERVICE_URL` |

## Endpoints (Spring Web)

- `GET /simulations/catalog`
- `POST /simulations`
- `POST /simulations/{id}/submit`

## Mensajes publicados (`RabbitTemplate`)

- `SimulationCompleted`

## Mensajes consumidos (`@RabbitListener`)

- `question.published` (guarda la pregunta en el catálogo)

## Ejecutar

```bash
mvn spring-boot:run
```

Necesita RabbitMQ en `localhost:5672` (ver el README de la raíz).
