# review-service

Asigna de 1 a 3 revisores, registra las evaluaciones y decide por unanimidad.

| Dato | Valor |
|---|---|
| Contexto delimitado | Revisión por Pares |
| Puerto | `8085` |
| Paquete | `co.edu.unicauca.review` |
| Persistencia | H2 en memoria propia (`review_db`) |
| Variables | `SERVER_PORT`, `RABBITMQ_HOST`, `RABBITMQ_PORT`, `RABBITMQ_USER`, `RABBITMQ_PASSWORD`, `DIRECT_EXCHANGE`, `TOPIC_EXCHANGE`, `LIFECYCLE_SERVICE_URL`, `USER_SERVICE_URL` |

## Endpoints (Spring Web)

- `POST /reviews/assign`
- `POST /reviews/{questionId}/evaluations`
- `GET /reviews/{questionId}`
- `GET /reviews/assigned?reviewerId=`

## Mensajes publicados (`RabbitTemplate`)

- `ReviewerAssigned` (uno por revisor, routing key `reviewer.assigned`)
- `EvaluationSubmitted`
- `ReviewCompleted`

## Mensajes consumidos (`@RabbitListener`)

- `question.submitted` (abre el caso y la ronda de revisión)

## Ejecutar

```bash
mvn spring-boot:run
```

Necesita RabbitMQ en `localhost:5672` (ver el README de la raíz).
