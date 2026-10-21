# lifecycle-service

Única autoridad sobre el estado de la pregunta y sus transiciones.

| Dato | Valor |
|---|---|
| Contexto delimitado | Ciclo de Vida de Preguntas |
| Puerto | `8084` |
| Paquete | `co.edu.unicauca.lifecycle` |
| Persistencia | H2 en memoria propia (`lifecycle_db`) |
| Variables | `SERVER_PORT`, `RABBITMQ_HOST`, `RABBITMQ_PORT`, `RABBITMQ_USER`, `RABBITMQ_PASSWORD`, `DIRECT_EXCHANGE`, `TOPIC_EXCHANGE` |

## Endpoints (Spring Web)

- `POST /lifecycle`: crea el estado Borrador
- `GET /lifecycle/{questionId}`
- `POST /lifecycle/{questionId}/transition` y `POST /lifecycle/transition`: transición síncrona pedida por otro servicio
- `GET /lifecycle/operations/{operationId}`: resultado de un comando asíncrono

## Mensajes publicados (`RabbitTemplate`)

- `OperationResult`
- `question.state-changed`
- `question.submitted`
- `question.published`

## Mensajes consumidos (`@RabbitListener`)

- Comandos de la cola `lifecycle.commands`

## Ejecutar

```bash
mvn spring-boot:run
```

Necesita RabbitMQ en `localhost:5672` (ver el README de la raíz).
