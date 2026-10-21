# question-service

Administra el contenido de la pregunta, valida por REST y pide los cambios de estado por RabbitMQ.

| Dato | Valor |
|---|---|
| Contexto delimitado | Gestión de Preguntas |
| Puerto | `8083` |
| Paquete | `co.edu.unicauca.question` |
| Persistencia | H2 en memoria propia (`question_db`) |
| Variables | `SERVER_PORT`, `RABBITMQ_HOST`, `RABBITMQ_PORT`, `RABBITMQ_USER`, `RABBITMQ_PASSWORD`, `DIRECT_EXCHANGE`, `TOPIC_EXCHANGE`, `VALIDATION_SERVICE_URL`, `LIFECYCLE_SERVICE_URL`, `USER_SERVICE_URL` |

## Endpoints (Spring Web)

- `POST /questions`: comprueba el autor en user-service, valida, crea el estado en lifecycle y responde 201 o 400
- `GET /questions`, `GET /questions/{id}`
- `PUT /questions/{id}`: solo en Borrador o Rechazada
- `POST /questions/{id}/submit`, `/publish`, `/archive`: 202 con `operationId`
- `GET /operations/{operationId}`

## Mensajes publicados (`RabbitTemplate`)

- Comandos `SubmitQuestionForReview`, `PublishQuestion` y `ArchiveQuestion` (exchange direct)

## Mensajes consumidos (`@RabbitListener`)

- `OperationResult` (cola `question.operation-result`)
- `question.state-changed`

## Ejecutar

```bash
mvn spring-boot:run
```

Necesita RabbitMQ en `localhost:5672` (ver el README de la raíz).
