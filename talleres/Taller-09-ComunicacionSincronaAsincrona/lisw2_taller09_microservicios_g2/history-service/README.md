# history-service

Guarda el historial de cada pregunta y la auditoría de accesos a partir de todos los eventos.

| Dato | Valor |
|---|---|
| Contexto delimitado | Administración y Trazabilidad |
| Puerto | `8087` |
| Paquete | `co.edu.unicauca.history` |
| Persistencia | H2 en memoria propia (`history_db`) |
| Variables | `SERVER_PORT`, `RABBITMQ_HOST`, `RABBITMQ_PORT`, `RABBITMQ_USER`, `RABBITMQ_PASSWORD`, `DIRECT_EXCHANGE`, `TOPIC_EXCHANGE` |

## Endpoints (Spring Web)

- `GET /history/{questionId}`: línea de tiempo
- `GET /history/audit?tipo=`

## Mensajes publicados (`RabbitTemplate`)

- Ninguno

## Mensajes consumidos (`@RabbitListener`)

- Todos los eventos del exchange de eventos (routing key `#`)

## Ejecutar

```bash
mvn spring-boot:run
```

Necesita RabbitMQ en `localhost:5672` (ver el README de la raíz).
