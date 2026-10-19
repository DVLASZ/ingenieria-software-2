# notification-service

Genera las notificaciones de los eventos sin bloquear el flujo principal.

| Dato | Valor |
|---|---|
| Contexto delimitado | Notificaciones |
| Puerto | `8086` |
| Paquete | `co.edu.unicauca.notification` |
| Persistencia | H2 en memoria propia (`notification_db`) |
| Variables | `SERVER_PORT`, `RABBITMQ_HOST`, `RABBITMQ_PORT`, `RABBITMQ_USER`, `RABBITMQ_PASSWORD`, `DIRECT_EXCHANGE`, `TOPIC_EXCHANGE` |

## Endpoints (Spring Web)

- `GET /notifications?destinatario=&tipo=`

## Mensajes publicados (`RabbitTemplate`)

- Ninguno

## Mensajes consumidos (`@RabbitListener`)

- `ReviewerAssigned`
- `EvaluationSubmitted`
- `ReviewCompleted`
- `ValidationFailed`
- `question.published`

## Ejecutar

```bash
mvn spring-boot:run
```

Necesita RabbitMQ en `localhost:5672` (ver el README de la raíz).
