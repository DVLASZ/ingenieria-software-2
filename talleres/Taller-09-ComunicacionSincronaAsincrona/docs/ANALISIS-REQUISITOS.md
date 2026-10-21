# Taller 09: análisis de requisitos

> Verificación final de cada requisito de los dos documentos: [`TRAZABILIDAD-REQUISITOS.md`](TRAZABILIDAD-REQUISITOS.md).
>
> El diseño de la solución (nueve microservicios, estados y reglas del proyecto, mensajería y estructura) está en
> [`DISENO-MICROSERVICIOS.md`](DISENO-MICROSERVICIOS.md).

Comparación del **enunciado** (`lisw2_taller09_ComunicacionesSincronasAsincronas.pdf`) y de la **guía paso a
paso** (`lisw2_guia_paso_a_paso_taller09-.pdf`). La profesora aclaró que la guía es solo orientadora y que lo
que vale es el enunciado; el grupo decidió cumplir **ambos** en lo que no se contradigan y, donde chocan,
seguir el enunciado.

## 1. Entregables

| Entregable | Detalle | Fuente |
|---|---|---|
| Repositorio público en GitHub | Monorepo con `user-service`, `question-service`, `validation-service`, `lifecycle-service`, `review-service` y `notification-service` | Ambos |
| `docker-compose.yml` | RabbitMQ con la consola de administración (5672 y 15672) | Guía |
| `/postman` | Colección `.json` con variables de entorno, organizada por microservicio, con casos exitosos y fallidos | Ambos |
| `.gitignore` | Ignora `target/`, `.mvn/`, `.idea/`, `.DS_Store` | Guía |
| Informe | `lisw2_taller09_g2.docx` en la plantilla `Formato-Documento.docx`, con el enlace al repositorio y capturas de RabbitMQ (`localhost:15672`) | Enunciado |
| Commits | Incrementales, identificando el microservicio | Guía |
| Equipo | Pareja (máximo 2): Edward y Laura | Enunciado |
| Sustentación | La próxima clase; la persona se elige al azar | Enunciado |

## 2. Servicios y puertos del taller

Coinciden en los dos documentos y en la Figura 2. El grupo añade tres más (ver el diseño).

| Servicio | Puerto | Responsabilidad | Comunicación |
|---|---|---|---|
| user-service | 8081 | Autenticar y devolver roles | REST |
| validation-service | 8082 | Validar estructura, opciones y clave | REST |
| question-service | 8083 | Contenido de la pregunta; coordina el flujo | REST y RabbitMQ (publica) |
| lifecycle-service | 8084 | Único dueño del estado y de las transiciones | RabbitMQ (consume) y REST |
| review-service | 8085 | Asignaciones y resultados de revisión | REST y RabbitMQ (publica) |
| notification-service | 8086 | Notificaciones sin bloquear | RabbitMQ (consume) |

## 3. Diferencias entre los dos documentos y decisión

| Tema | Enunciado | Guía | Decisión |
|---|---|---|---|
| Estados | DRAFT, PENDING_REVIEW, IN_REVIEW, REJECTED (con motivo); la Figura 1 añade APPROVED, PUBLISHED y ARCHIVED | DRAFT, PENDING_REVIEW, IN_REVIEW, APPROVED | Los **siete del proyecto** (equivalen a la unión), con sus nombres y transiciones, más el equivalente en inglés |
| Token | Paso 1: «no se implementan tokens ni JWT». Paso 2: el token va en `Authorization` | No lo menciona | **Pendiente de la profesora**; por defecto, valor simple sin validar |
| Exchange y colas | Figura 2: `lifecycle.commands`, `question.operation-result`, `notification.reviewer-assigned`; routing keys `question.submit`, `question.operation-result`, `reviewer.assigned` | Direct exchange `saberpro.direct.exchange`; cola `question.submit.review.queue` | Exchange direct `saberpro.direct.exchange` (nombre de la guía) con las **colas y routing keys de la Figura 2**; los eventos van por un exchange topic aparte |
| Resultado de la operación | `GET /operations/{operationId}` en Question Service; Lifecycle publica `OperationResult` | Lifecycle registra el resultado | Lifecycle registra el resultado y lo publica; Question Service lo guarda y lo expone |
| Consulta de estado | Review pregunta `GET /lifecycle/{questionId}` | `GET /lifecycle/{id}` | Igual |
| Transición | `POST /lifecycle/{questionId}/transition` | `POST /lifecycle/transition` | Se usa la ruta del enunciado (con el id en la ruta) |
| Persistencia | No dice | Una base por servicio (H2, SQLite o PostgreSQL) | **H2 en memoria por servicio**, sin compartir tablas |
| Contextos | «Definan y diseñen sus propios contextos»; los seis son una sugerencia | Seis servicios | **Nueve** servicios: los seis y tres más del proyecto |
| Errores REST | 400 con los errores de validación | `@ExceptionHandler` y 400 | Igual, con un manejador global |
| Formato de mensajes | No dice | `Jackson2JsonMessageConverter` | JSON en todos los mensajes |
| Cliente | «Aplicación de escritorio» | Postman | Postman (confirmar con la profesora) |

## 4. Flujo del escenario

1. `POST /auth/login` en user-service. Credenciales correctas: 200 con usuario y roles; incorrectas: 401.
2. `POST /questions` en question-service con `Authorization`. Llama a `POST /validate` en validation-service.
   Válida: persiste en DRAFT y responde 201. Inválida: 400 con los errores, sin registrar la pregunta como
   válida y con un evento `ValidationFailed` para notificaciones.
3. `POST /questions/{id}/submit`: publica `SubmitQuestionForReview` (con `operationId`) y responde **202**
   con el `operationId`. No garantiza que la transición haya terminado.
4. lifecycle-service consume el comando, verifica DRAFT→PENDING_REVIEW, registra el estado y el resultado
   (COMPLETED o FAILED con mensaje) y publica `OperationResult` y `QuestionSubmittedForReview`.
5. `GET /operations/{operationId}` en question-service muestra PENDING, COMPLETED o FAILED.
6. `POST /reviews/assign` en review-service: consulta `GET /lifecycle/{id}`; si está en PENDING_REVIEW, pide
   `POST /lifecycle/{id}/transition` a IN_REVIEW; solo si tiene éxito registra la asignación y publica
   `ReviewerAssigned`.
7. notification-service consume `ReviewerAssigned` y simula el envío al revisor sin bloquear la asignación.

## 5. Modelo (Figura 1)

- **Mensajes:** comando `SubmitQuestionForReview` (operationId, questionId, requestedBy, requestedAt) y
  eventos `QuestionSubmittedForReview`, `ReviewerAssigned` (questionId, reviewerId, assignedBy, occurredAt),
  `ValidationFailed` y `OperationResult`.
- **Enumeraciones:** `QuestionStatus` (siete estados), `OperationStatus` (PENDING, COMPLETED, FAILED),
  `ReviewResult` (APPROVED, REJECTED) y `NotificationType` (REVIEWER_ASSIGNED, VALIDATION_FAILED,
  QUESTION_APPROVED, QUESTION_REJECTED).
- **Entidades por servicio:** User; Question y Option; QuestionState y OperationResult; ReviewAssignment y
  Review; Notification; ValidationRule.

## 6. Matriz de pruebas (11 casos para Postman)

| Caso | Resultado esperado |
|---|---|
| Login con credenciales válidas | 200, autenticación exitosa |
| Login con credenciales incorrectas | 401, acceso rechazado |
| Crear pregunta válida | 201, persistida en DRAFT |
| Crear pregunta inválida | 400, error informado y no se persiste como válida |
| Enviar pregunta a revisión | 202; luego la transición a PENDING_REVIEW queda COMPLETED |
| Aprobar una pregunta en DRAFT | Transición rechazada |
| Asignar un revisor a una pregunta elegible | Asignación registrada y notificación generada |
| Rechazar una pregunta en IN_REVIEW | Estado REJECTED y motivo registrado |
| Corregir una pregunta rechazada | Puede volver al proceso de revisión |
| La validación falla al crear una pregunta | Error informado; la pregunta no se registra como válida |
| Falla el procesamiento de una transición asíncrona | El cliente consulta el resultado (FAILED) y no se informa que se completó |

## 7. Rúbrica y cómo la cubrimos

| Criterio | Peso | Cómo se cubre |
|---|---|---|
| Diseño y responsabilidades | 15 % | Seis servicios con una responsabilidad clara; Lifecycle como única autoridad del estado; diagramas de contextos y de secuencia en el informe |
| Comunicación REST | 20 % | Login, validación, creación, consulta de estado y operaciones; `RestClient` entre servicios; códigos HTTP correctos |
| Comunicación RabbitMQ | 25 % | Comando, eventos y resultado por exchange direct; `@RabbitListener`; JSON; 202 Accepted |
| Estados, errores y resiliencia | 15 % | Máquina de estados con transiciones válidas; falla visible en `GET /operations`; asignación consistente; cola de mensajes fallidos y reintentos |
| Postman y evidencias | 15 % | Colección con variables, un folder por servicio, casos de éxito y de error; capturas de RabbitMQ |
| Documentación y sustentación | 10 % | Informe en la plantilla, README por servicio y guía de repaso para sustentar |

## 8. Mejoras opcionales para llegar al máximo

- Cola de mensajes fallidos (DLQ) y reintentos limitados en el consumidor de Lifecycle.
- Consumidores idempotentes (el mismo comando dos veces no repite la transición).
- Pruebas JUnit por servicio, además de la colección de Postman.
- Todo debe funcionar también **sin Docker**: los servicios se arrancan con Maven y solo necesitan un RabbitMQ
  en `localhost:5672`, con Docker o instalado en el equipo.
