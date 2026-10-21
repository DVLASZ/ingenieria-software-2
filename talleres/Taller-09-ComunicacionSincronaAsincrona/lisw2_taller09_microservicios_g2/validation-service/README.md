# validation-service

Valida la estructura de una pregunta con las reglas del proyecto.

| Dato | Valor |
|---|---|
| Contexto delimitado | Validación de Preguntas |
| Puerto | `8082` |
| Paquete | `co.edu.unicauca.validation` |
| Persistencia | Ninguna |
| Variables | `SERVER_PORT`, `RABBITMQ_HOST`, `RABBITMQ_PORT`, `RABBITMQ_USER`, `RABBITMQ_PASSWORD`, `DIRECT_EXCHANGE`, `TOPIC_EXCHANGE` |

## Endpoints (Spring Web)

- `POST /validate`: `{ valid, errors[] }`

## Mensajes publicados (`RabbitTemplate`)

- `ValidationFailed` (routing key `validation.failed`)

## Mensajes consumidos (`@RabbitListener`)

- Ninguno

## Ejecutar

```bash
mvn spring-boot:run
```

Necesita RabbitMQ en `localhost:5672` (ver el README de la raíz).
