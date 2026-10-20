# Taller 09: referencia de la API y de los mensajes

Todos los cuerpos son JSON. Los errores tienen siempre la forma `{ "message": "...", "errors": ["..."] }`. Las
direcciones base son `http://localhost:<puerto>`.

## Códigos de respuesta comunes

| Código | Cuándo |
|---|---|
| 200 | Consulta o transición correcta |
| 201 | Recurso creado |
| 202 | Solicitud asíncrona aceptada (aún no necesariamente procesada) |
| 400 | Datos inválidos, pregunta que no pasa la validación, regla de negocio incumplida |
| 401 | Credenciales incorrectas, o falta el encabezado `Authorization` |
| 403 | El usuario no tiene el rol necesario |
| 404 | No existe el recurso |
| 409 | La acción no es válida en el estado actual (transición no permitida, ya evaluado, etc.) |
| 503 | Un servicio o RabbitMQ no está disponible |

## user-service (8081)

| Método y ruta | Descripción | Respuestas |
|---|---|---|
| `POST /auth/login` | Cuerpo `{ username, password }`. Devuelve `{ authenticated, message, user{ id, username, nombreCompleto, email, roles }, token }` | 200, 401, 400 |
| `GET /users/{id}` | Datos y roles de un usuario | 200, 404 |
| `GET /users?role=REVISOR&excluir={id}` | Usuarios con un rol (`AUTOR`, `REVISOR` o `ADMINISTRADOR`), sin el excluido | 200 |

Usuarios precargados (clave `clave123`): `admin`, `autor1`, `autor2`, `revisor1` a `revisor4`. Las claves se guardan con hash.

## validation-service (8082)

| Método y ruta | Descripción | Respuestas |
|---|---|---|
| `POST /validate` | Cuerpo `{ autorId, titulo, contexto, pregunta, opciones[4], respuestaCorrecta }`. Devuelve `{ valid, errors[] }` | 200 siempre |

Reglas: contexto presente, una única pregunta directa (un solo signo de interrogación), exactamente cuatro opciones
no vacías, distintas, de máximo 300 caracteres y sin «todas/ninguna de las anteriores», y una respuesta correcta entre A, B, C y D.

## question-service (8083)

| Método y ruta | Descripción | Respuestas |
|---|---|---|
| `POST /questions` | Crea la pregunta (cuerpo en la tabla de abajo). Exige `Authorization`. Comprueba el rol AUTOR (user-service), valida (validation-service) y crea el estado (lifecycle-service) | 201, 400, 401, 403, 503 |
| `GET /questions?estado=&competencia=&tema=&autorId=&page=&size=` | Lista paginada y filtrable | 200 |
| `GET /questions/{id}` | Detalle, con `estado`, `etiqueta` y `equivalente` | 200, 404 |
| `PUT /questions/{id}` | Edita; solo en Borrador o Rechazada (esta última vuelve a Borrador). Exige `Authorization`; solo el autor | 200, 400, 401, 403, 409 |
| `POST /questions/{id}/submit` | Publica el comando `SubmitQuestionForReview`. Cuerpo opcional `{ requestedBy, reason }` | 202, 401, 404, 503 |
| `POST /questions/{id}/publish` | Publica el comando `PublishQuestion` | 202, 401, 404, 503 |
| `POST /questions/{id}/archive` | Publica el comando `ArchiveQuestion` | 202, 401, 404, 503 |
| `GET /operations/{operationId}` | `{ operationId, questionId, tipo, status, mensaje }`; `status` es `PENDING`, `COMPLETED` o `FAILED` | 200, 404 |

Cuerpo de la pregunta: `autorId`, `titulo`, `contexto`, `pregunta`, `opciones` (cuatro textos), `respuestaCorrecta`
(`A` a `D`), `justificacion`, `bibliografia`, `competencia`, `tema`, `subtema`, `dificultad`.

La respuesta 202 es `{ "message": "...", "operationId": "<uuid>", "status": "PROCESSING" }`.

## lifecycle-service (8084)

| Método y ruta | Descripción | Respuestas |
|---|---|---|
| `POST /lifecycle` | Cuerpo `{ questionId, autorId, titulo, competencia }`; crea el estado `BORRADOR` | 201, 409 |
| `GET /lifecycle/{questionId}` | Estado actual, equivalente en inglés e historial de transiciones | 200, 404 |
| `POST /lifecycle/{questionId}/transition` | Cuerpo `{ to, requestedBy, motivo }`. Rechazar exige `motivo` | 200, 400, 404, 409 |
| `POST /lifecycle/transition` | Igual, con `questionId` en el cuerpo (ruta de la guía) | 200, 400, 404, 409 |
| `GET /lifecycle/operations/{operationId}` | Resultado de un comando asíncrono | 200, 404 |

Transiciones válidas: `BORRADOR→PENDIENTE_REVISION→EN_REVISION→APROBADA|RECHAZADA`, `RECHAZADA→BORRADOR`,
`APROBADA→PUBLICADA` y cualquier estado `→ARCHIVADA`.

| Estado | Equivalente |
|---|---|
| `BORRADOR` | `DRAFT` |
| `PENDIENTE_REVISION` | `PENDING_REVIEW` |
| `EN_REVISION` | `IN_REVIEW` |
| `APROBADA` | `APPROVED` |
| `RECHAZADA` | `REJECTED` |
| `PUBLICADA` | `PUBLISHED` |
| `ARCHIVADA` | `ARCHIVED` |

## review-service (8085)

| Método y ruta | Descripción | Respuestas |
|---|---|---|
| `POST /reviews/assign` | Cuerpo `{ questionId, reviewerIds[1..3], assignedBy }`. Solo un administrador; el autor no puede ser revisor; sin repetidos; la pregunta debe estar `PENDIENTE_REVISION` | 201, 400, 403, 404, 409 |
| `POST /reviews/{questionId}/evaluations` | Cuerpo `{ reviewerId, decision: APPROVE\|REJECT, observation }`. Rechazar exige observación. Un rechazo cierra la ronda; si todos aprueban, queda aprobada | 200, 400, 403, 409 |
| `GET /reviews/{questionId}` | Todas las rondas con asignaciones y observaciones | 200, 404 |
| `GET /reviews/assigned?reviewerId=` | Revisiones pendientes de un revisor | 200 |

## notification-service (8086)

| Método y ruta | Descripción |
|---|---|
| `GET /notifications?destinatario=&tipo=` | Notificaciones generadas, de la más reciente a la más antigua. Tipos: `REVIEWER_ASSIGNED`, `EVALUATION_SUBMITTED`, `QUESTION_APPROVED`, `QUESTION_REJECTED`, `VALIDATION_FAILED`, `QUESTION_PUBLISHED` |

## history-service (8087)

| Método y ruta | Descripción |
|---|---|
| `GET /history/{questionId}` | Línea de tiempo de la pregunta |
| `GET /history/audit?tipo=&page=&size=` | Todos los eventos (inicios de sesión incluidos), filtrables por tipo |

## simulation-service (8088)

| Método y ruta | Descripción | Respuestas |
|---|---|---|
| `GET /simulations/catalog` | Preguntas publicadas disponibles (sin la respuesta correcta) | 200 |
| `POST /simulations` | Cuerpo `{ userId, cantidad }`; arma un simulacro con las primeras N preguntas del catálogo | 201, 400, 409 |
| `POST /simulations/{id}/submit` | Cuerpo `{ respuestas: [{ questionId, respuesta }] }`; califica y publica el resultado | 200, 400, 404, 409 |

## tracking-service (8089)

| Método y ruta | Descripción | Respuestas |
|---|---|---|
| `GET /tracking/users/{id}` | Simulacros, promedio, aciertos y porcentaje de acierto | 200, 404 |
| `GET /tracking/summary` | Usuarios, simulacros y promedio general | 200 |

## Mensajes de RabbitMQ

Dos exchanges, ambos durables: `saberpro.direct.exchange` (*direct*, comandos y resultado de operaciones) y
`saberpro.topic.exchange` (*topic*, eventos). Los mensajes viajan en JSON con un sobre común:

```json
{ "eventId": "uuid", "type": "ReviewerAssigned", "occurredAt": "2026-10-16T18:00:00Z", "data": { } }
```

| Mensaje (`type`) | Lo publica | Exchange y routing key | Cola | Lo consume |
|---|---|---|---|---|
| `SubmitQuestionForReview` | question | direct, `question.submit` | `lifecycle.commands` | lifecycle |
| `PublishQuestion` | question | direct, `question.publish` | `lifecycle.commands` | lifecycle |
| `ArchiveQuestion` | question | direct, `question.archive` | `lifecycle.commands` | lifecycle |
| `OperationResult` | lifecycle | direct, `question.operation-result` | `question.operation-result` | question |
| `question.state-changed` | lifecycle | topic, `question.state-changed` | `question.state-changed`, `history.events` | question, history |
| `QuestionSubmittedForReview` | lifecycle | topic, `question.submitted` | `review.submitted-questions`, `history.events` | review, history |
| `question.published` | lifecycle | topic, `question.published` | `simulation.published-questions`, `notification.events`, `history.events` | simulation, notification, history |
| `ValidationFailed` | validation | topic, `validation.failed` | `notification.events`, `history.events` | notification, history |
| `ReviewerAssigned` | review | topic, `reviewer.assigned` | `notification.reviewer-assigned`, `history.events` | notification, history |
| `EvaluationSubmitted` | review | topic, `review.evaluation-submitted` | `notification.events`, `history.events` | notification, history |
| `ReviewCompleted` | review | topic, `review.completed` | `notification.events`, `history.events` | notification, history |
| `auth.login-succeeded`, `auth.login-failed` | user | topic, `auth.login-*` | `history.events` | history |
| `SimulationCompleted` | simulation | topic, `simulation.completed` | `tracking.simulation-results`, `history.events` | tracking, history |

Cuerpo (`data`) de los mensajes principales:

| Mensaje | Campos |
|---|---|
| `SubmitQuestionForReview` y demás comandos | `operationId`, `questionId`, `requestedBy`, `requestedAt`, `reason` |
| `OperationResult` | `operationId`, `questionId`, `type`, `status` (`COMPLETED` o `FAILED`), `message` |
| `QuestionSubmittedForReview` | `questionId`, `authorId`, `title`, `competency`, `operationId`, `newState` |
| `ReviewerAssigned` | `questionId`, `authorId`, `title`, `round`, `reviewerId`, `assignedBy` |
| `EvaluationSubmitted` | `questionId`, `authorId`, `title`, `round`, `reviewerId`, `decision`, `observation`, `adminId` |
| `ReviewCompleted` | `questionId`, `authorId`, `title`, `round`, `result` (`APPROVED` o `REJECTED`), `observations[]`, `adminId` |
| `ValidationFailed` | `authorId`, `title`, `errors[]` |
| `SimulationCompleted` | `simulationId`, `userId`, `correct`, `total`, `score`, `byCompetency` |

### Garantías

- **Idempotencia:** un comando o un evento repetido no se procesa dos veces (por `operationId` o `eventId`).
- **Reintentos:** si un consumidor falla, se reintenta (hasta 3 intentos, con espera creciente); después el mensaje se descarta
  (la cola de mensajes fallidos queda fuera de esta versión).
- **Avisos que no bloquean:** si RabbitMQ no está disponible, los eventos informativos solo se registran en el log y la
  operación principal termina; los comandos devuelven 503 porque sin ellos la solicitud no se puede aceptar.
