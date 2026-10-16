# Taller 09: comunicación síncrona y asíncrona entre microservicios

Grupo 8 (Edward Esteban Dávila Salazar y Laura Isabel Sánchez Fernández). Laboratorio de Ingeniería de
Software II, Universidad del Cauca.

Nueve microservicios Spring Boot que gestionan el ciclo de vida de una pregunta del Banco de Preguntas Saber PRO.
Es una versión reducida del proyecto del curso: **los mismos siete estados, las mismas reglas de revisión y el mismo
mecanismo de rechazo**. Usan REST (Spring Web) cuando necesitan respuesta inmediata y RabbitMQ (`RabbitTemplate` para
publicar, `@RabbitListener` para consumir) para lo que no debe bloquear.

## Los nueve servicios

| Servicio | Puerto | Contexto | Qué hace |
|---|---|---|---|
| [`user-service`](user-service/) | 8081 | Usuarios | Login (`POST /auth/login`), usuarios y revisores elegibles |
| [`validation-service`](validation-service/) | 8082 | Validación | `POST /validate` con las reglas estructurales del proyecto |
| [`question-service`](question-service/) | 8083 | Preguntas | Crea y edita preguntas; pide los cambios de estado con comandos (202 Accepted) |
| [`lifecycle-service`](lifecycle-service/) | 8084 | Ciclo de vida | Único dueño del estado; procesa los comandos y las transiciones |
| [`review-service`](review-service/) | 8085 | Revisión | Asigna de 1 a 3 revisores, recibe evaluaciones y decide por unanimidad |
| [`notification-service`](notification-service/) | 8086 | Notificaciones | Genera avisos de los eventos sin bloquear nada |
| [`history-service`](history-service/) | 8087 | Trazabilidad | Historial por pregunta y auditoría, a partir de todos los eventos |
| [`simulation-service`](simulation-service/) | 8088 | Simulacros | Catálogo de preguntas publicadas y simulacros resueltos |
| [`tracking-service`](tracking-service/) | 8089 | Seguimiento | Estadísticas por usuario a partir de los simulacros |

## Documentación

| Documento | Contenido |
|---|---|
| [`DISENO-MICROSERVICIOS.md`](../docs/DISENO-MICROSERVICIOS.md) | Contextos, estados y reglas, mensajería, flujos y decisiones de diseño |
| [`API.md`](../docs/API.md) | Endpoints de cada servicio y mensajes de RabbitMQ |
| [`TRAZABILIDAD-REQUISITOS.md`](../docs/TRAZABILIDAD-REQUISITOS.md) | Cada requisito del enunciado, del Classroom y de la guía, y dónde se cumple |
| [`GUIA-DE-PRUEBAS.md`](../docs/GUIA-DE-PRUEBAS.md) | Cómo ejecutar y comprobar todo, y qué capturar para el informe |
| [`ANALISIS-REQUISITOS.md`](../docs/ANALISIS-REQUISITOS.md) | Comparación inicial del enunciado y la guía |
| [`PLAN-DE-COMMITS.md`](../docs/PLAN-DE-COMMITS.md) | Commits incrementales por microservicio |

Cada servicio tiene además su propio README con sus endpoints, mensajes y variables.

## Estructura

```
lisw2_taller09_microservicios_g8/
├── pom.xml                  compila los nueve servicios de una vez
├── docker-compose.yml       RabbitMQ con la consola de administración
├── scripts/                 levantar.sh/.ps1 y detener.sh/.ps1
├── postman/                 colección y entorno de Postman
├── user-service/ … tracking-service/   un proyecto Spring Boot por servicio
```

Cada servicio tiene el mismo esquema de paquetes: `config` (RabbitMQ), `controller` (Spring Web), `dto`,
`messaging` (`*Publisher` y `*Listener`), `model`, `repository` y `service`. No comparten código: cada uno trae su
propio `RabbitMQConfig` y sus propios DTO.

## Requisitos

- Java 21 y Maven 3.9 o superior.
- RabbitMQ en `localhost:5672`: con Docker (`docker compose up -d`) o instalado en el equipo.

## Compilar y probar

```bash
mvn clean package        # compila los nueve servicios y corre sus 78 pruebas (no necesitan RabbitMQ)
```

## Ejecutar

Con los scripts (levantan RabbitMQ con Docker si no responde, y los nueve servicios):

```bash
./scripts/levantar.sh              # en PowerShell: .\scripts\levantar.ps1
./scripts/detener.sh --rabbit      # en PowerShell: .\scripts\detener.ps1 -Rabbit
```

Si RabbitMQ ya está instalado en el equipo (sin Docker): `./scripts/levantar.sh --sin-docker`
(`.\scripts\levantar.ps1 -SinDocker`).

A mano, un servicio por terminal: `mvn spring-boot:run` desde su carpeta. Los registros de los scripts quedan en
`logs/`. La consola de RabbitMQ está en http://localhost:15672 (usuario y clave `guest`).

Cada servicio se configura con variables de entorno: `SERVER_PORT`, `RABBITMQ_HOST`, `RABBITMQ_PORT`,
`RABBITMQ_USER`, `RABBITMQ_PASSWORD`, los nombres de los exchanges (`DIRECT_EXCHANGE`, `TOPIC_EXCHANGE`) y las
direcciones de los servicios a los que llama (`VALIDATION_SERVICE_URL`, `LIFECYCLE_SERVICE_URL`, `USER_SERVICE_URL`,
`QUESTION_SERVICE_URL`). `question-service` y `review-service` consultan a `user-service`.

## Probar el flujo completo

Importar en Postman `postman/SaberPro_Microservices.postman_collection.json` y el entorno
`postman/SaberPro_Local.postman_environment.json`, y ejecutar la colección completa con el Collection Runner. O por
consola:

```bash
npx newman run postman/SaberPro_Microservices.postman_collection.json -e postman/SaberPro_Local.postman_environment.json
```

La colección tiene 67 peticiones con 146 verificaciones, organizadas por microservicio, y cubre los casos de éxito y de
error del enunciado. Se puede ejecutar varias veces seguidas sin reiniciar los servicios.

## Usuarios de prueba

Todos con la contraseña `clave123`: `admin` (administrador), `autor1` y `autor2` (autores) y `revisor1` a `revisor4`
(revisores).

## Qué queda fuera de esta versión

Keycloak y JWT (el login devuelve un token simple y se exige el encabezado `Authorization`), Eureka, API Gateway, el
patrón Outbox, la cola de mensajes fallidos y PostgreSQL (cada servicio usa su propia base H2 en memoria).
