# user-service

Autentica usuarios y entrega sus roles; publica los eventos de inicio de sesión.

| Dato | Valor |
|---|---|
| Contexto delimitado | Gestión de Usuarios |
| Puerto | `8081` |
| Paquete | `co.edu.unicauca.user` |
| Persistencia | H2 en memoria propia (`user_db`) |
| Variables | `SERVER_PORT`, `RABBITMQ_HOST`, `RABBITMQ_PORT`, `RABBITMQ_USER`, `RABBITMQ_PASSWORD`, `DIRECT_EXCHANGE`, `TOPIC_EXCHANGE` |

## Endpoints (Spring Web)

- `POST /auth/login`: 200 con usuario, roles y token simple, o 401
- `GET /users/{id}`
- `GET /users?role=REVISOR&excluir={id}`: usuarios con un rol

## Mensajes publicados (`RabbitTemplate`)

- `auth.login-succeeded`
- `auth.login-failed`

## Mensajes consumidos (`@RabbitListener`)

- Ninguno

## Ejecutar

```bash
mvn spring-boot:run
```

Necesita RabbitMQ en `localhost:5672` (ver el README de la raíz).
