# Taller 09: trazabilidad de requisitos

Revisión requisito por requisito del **enunciado** (`lisw2_taller09_ComunicacionesSincronasAsincronas.pdf`), del
**mensaje de la docente en el Classroom** y de la **guía paso a paso** (`lisw2_guia_paso_a_paso_taller09-.pdf`).
Cada fila dice dónde se cumple y con qué evidencia. Rutas relativas a `lisw2_taller09_microservicios_g8/`.

Estados: **Cumple** · **Cumple con diferencia** (hay una decisión justificada en la sección 5) · **Pendiente**
(entregable que se hace al final: el informe).

Evidencia automática: `mvn clean package` corre 78 pruebas unitarias y de integración, y la colección de Postman
(67 peticiones, 146 verificaciones) pasa completa contra los nueve servicios y RabbitMQ reales.

## 1. Enunciado

### Objetivo y descripción

| # | Requisito | Dónde y cómo se cumple | Estado |
|---|---|---|---|
| 1 | Microservicios con Spring Boot, API REST, RabbitMQ y Postman | Nueve proyectos Spring Boot 3.5 (Java 21); REST con Spring Web; RabbitMQ con Spring AMQP; colección en `postman/` | Cumple |
| 2 | Comunicación síncrona (REST) y asíncrona (RabbitMQ) entre servicios | REST: question→validation, question→user, question→lifecycle, review→user, review→lifecycle, simulation→question. RabbitMQ: comandos, resultados y eventos (ver `docs/API.md`) | Cumple |
| 3 | Aplicarlo al proyecto del curso, evolución del monolito | Versión reducida del proyecto: mismos siete estados, reglas de revisión y rechazo | Cumple |
| 4 | Definir y diseñar los propios contextos delimitados | `docs/DISENO-MICROSERVICIOS.md`: nueve contextos (los seis sugeridos más historial, simulacros y seguimiento) | Cumple |
| 5 | Los seis microservicios sugeridos y su responsabilidad | `user-service`, `question-service`, `validation-service`, `lifecycle-service`, `review-service`, `notification-service` | Cumple |
| 6 | Lifecycle es el único que modifica el estado; los demás solicitan transiciones o reaccionan a eventos | Solo `LifecycleService` cambia el estado. `question-service` publica comandos, `review-service` pide transiciones por REST, y la copia del estado de question solo se actualiza con el evento `question.state-changed` (o tras la aprobación de lifecycle al corregir) | Cumple |
| 7 | Usar Spring Web para los endpoints REST | Clases `@RestController` en `controller/` de cada servicio | Cumple |
| 8 | Usar Spring AMQP y `RabbitTemplate` para publicar | Clases `*Publisher` con `RabbitTemplate.convertAndSend(...)` (ver tabla de `DISENO-MICROSERVICIOS.md`, sección 3) | Cumple |
| 9 | Usar `@RabbitListener` para consumir | Clases `*Listener` con `@RabbitListener(queues = ...)` | Cumple |
| 10 | Puertos diferentes | `server.port: ${SERVER_PORT:808x}`, del 8081 al 8089 | Cumple |
| 11 | Variables de configuración por servicio | `application.yml` de cada servicio: `SERVER_PORT`, `RABBITMQ_*`, `DIRECT_EXCHANGE`, `TOPIC_EXCHANGE` y las direcciones de los servicios que llama | Cumple |

### Escenario 1

| # | Requisito | Dónde y cómo se cumple | Evidencia | Estado |
|---|---|---|---|---|
| 12 | Paso 1: `POST /auth/login` a User Service; devuelve JSON con el resultado y, si es exitosa, datos básicos y roles | `AuthController`; `LoginResponse(authenticated, message, user, token)` | Postman 01 «Login válido»; `UserServiceTest` | Cumple |
| 13 | Credenciales incorrectas: 401 Unauthorized | `AuthController` | Postman 01 «Login con credenciales incorrectas» | Cumple |
| 14 | Sin tokens de acceso ni JWT en esta versión | El login devuelve un token simple (UUID) sin firma ni validación | Acordado con la docente | Cumple con diferencia (5.4) |
| 15 | Paso 2: `POST /questions` con contenido, opciones y respuesta correcta; token en `Authorization` | `QuestionController.crear`; sin el encabezado responde 401 | Postman 03 «Crear pregunta válida» y «sin el encabezado» | Cumple |
| 16 | Paso 3: Question pide la validación a Validation por REST | `ValidationClient` → `POST /validate` | `QuestionServiceTest` | Cumple |
| 17 | Validación exitosa: persiste en DRAFT y responde 201 | `QuestionService.crear`; estado `BORRADOR` (equivalente `DRAFT`) | Postman 03 | Cumple |
| 18 | Validación fallida: 400 con los errores y no se registra como válida | `ApiException` 400 con `errors[]`; no se guarda la pregunta | Postman 03 «inválida»; prueba que cuenta las preguntas | Cumple |
| 19 | Paso 4: publicar en RabbitMQ el comando `SubmitQuestionForReview` con el id de la pregunta y un identificador único de operación, dirigido a Lifecycle | `CommandPublisher` (exchange direct, routing key `question.submit`); `operationId` = UUID | `QuestionServiceTest` | Cumple |
| 20 | Paso 5: 202 Accepted con el identificador de la operación, sin garantizar que la transición terminó | `QuestionController` responde `{message, operationId, status: PROCESSING}` | Postman 03 «Enviar … (202)» | Cumple |
| 21 | Paso 6: Lifecycle consume el comando, verifica DRAFT→PENDING_REVIEW y registra el estado | `CommandListener` + `LifecycleService.procesarComando` | `LifecycleServiceTest` | Cumple |
| 22 | Registrar el resultado para verificarlo en Postman (completó o falló) | Lifecycle guarda `OperationResult` y lo publica; question lo guarda y lo expone en `GET /operations/{id}` | Postman 03 «Resultado de la operación» (COMPLETED) y «la transición falla» (FAILED) | Cumple |
| 23 | Paso 7: el administrador pide a Review asignar un revisor a una pregunta en PENDING_REVIEW | `POST /reviews/assign` (solo administradores; de 1 a 3 revisores) | Postman 05 | Cumple (5.9) |
| 24 | Verificar elegibilidad de la pregunta y del revisor | Estado `PENDIENTE_REVISION` (consulta a lifecycle), revisor con rol REVISOR (consulta a user-service), el autor no puede ser revisor, sin repetidos | Postman 05 (casos de error); `ReviewServiceTest` | Cumple |
| 25 | Registrar la asignación y pedir a Lifecycle PENDING_REVIEW→IN_REVIEW | `ReviewService.asignar` | Postman 05 «Asignar dos revisores» | Cumple |
| 26 | Contemplar los fallos para evitar inconsistencias entre la asignación y el estado | Una sola transacción: se guarda la asignación, se pide la transición y, si falla, se revierte todo | `siLifecycleRechazaLaTransicionNoQuedaNingunaAsignacion` | Cumple |
| 27 | Paso 8: Review publica `ReviewerAssigned`; Notification lo consume y simula o hace el envío; no bloquea | `ReviewEventPublisher` (un evento por revisor); `NotificationListener`; `EmailSimulator` en el hilo del consumidor; si el broker falla, solo se registra | Postman 09; `NotificationServiceTest` | Cumple |

### Figuras, Postman y matriz de pruebas

| # | Requisito | Dónde y cómo se cumple | Estado |
|---|---|---|---|
| 28 | Figuras 1 y 2 como guía (cada estudiante puede proponer su diseño) | Se siguieron: servicios, DTO, mensajes, exchange, colas y routing keys de la Figura 2, tipos de notificación. Diferencias en 5.5 y 5.6 | Cumple con diferencia |
| 29 | Colección de Postman (o Bruno) con casos exitosos y fallidos | `postman/SaberPro_Microservices.postman_collection.json` | Cumple |
| 30 | Peticiones organizadas por microservicio | 12 carpetas, cada una con peticiones de un solo servicio (algunos aparecen dos veces porque el escenario vuelve a ellos) | Cumple |
| 31 | Demostrar los resultados de lo asíncrono con consultas posteriores | `GET /operations/{id}`, `GET /lifecycle/{id}`, `GET /notifications`, `GET /history/{id}` | Cumple |

Matriz de pruebas del enunciado (carpetas de la colección entre paréntesis):

| Caso | Resultado esperado | Petición de Postman |
|---|---|---|
| Login con credenciales válidas | Autenticación exitosa | 01 «Login válido (autor1)» |
| Login con credenciales incorrectas | Acceso rechazado | 01 «Login con credenciales incorrectas» (401) |
| Crear pregunta válida | Persistida en DRAFT | 03 «Crear pregunta válida (pregunta 1)» (201, `DRAFT`) |
| Crear pregunta inválida | Error informado y no persistida como válida | 03 «Crear pregunta inválida» (400) |
| Enviar pregunta a revisión | Transición a PENDING_REVIEW | 03 «Enviar … (202)» y «Resultado de la operación 1» (COMPLETED) |
| Aprobar una pregunta en DRAFT | Transición rechazada | 04 «Aprobar una pregunta en Borrador» (409), 04 «… (ruta de la guía)» y 05 «Aprobar una pregunta que no está en revisión» (409) |
| Asignar un revisor a una pregunta elegible | Asignación registrada y notificación generada | 05 «Asignar dos revisores a la pregunta 1» (201) y 09 «Notificaciones del revisor 1» |
| Rechazar una pregunta en IN_REVIEW | Estado REJECTED y motivo registrado | 05 «Revisor rechaza con observación»; 06 «Quedó Rechazada»; 07 «Lifecycle guardó el motivo» |
| Corregir una pregunta rechazada | Puede volver al proceso de revisión | 06 «Corregir la pregunta rechazada», «Reenviar…»; 08 «Se abrió la segunda ronda» |
| La validación falla al crear una pregunta | Error informado; no se registra como válida | 02 «Validar una pregunta inválida» y 03 «Crear pregunta inválida» |
| Falla el procesamiento de una transición asíncrona | El cliente consulta el resultado y no se informa falsamente que se completó | 03 «Enviar la pregunta 1 otra vez» y «Resultado: la transición falla» (FAILED) |

### Rúbrica

| Criterio | Peso | Evidencia |
|---|---|---|
| Diseño de microservicios y delimitación de responsabilidades | 15 % | `docs/DISENO-MICROSERVICIOS.md`: nueve contextos, un dueño del estado, base propia por servicio, sin código compartido |
| Comunicación síncrona REST | 20 % | Seis integraciones por REST con tiempos de espera, códigos HTTP y errores propagados; `docs/API.md` |
| Comunicación asíncrona con RabbitMQ | 25 % | Dos exchanges, nueve colas, comandos 202 + resultado, eventos, consumidores idempotentes, JSON; panel de RabbitMQ |
| Validación de estados, errores y resiliencia | 15 % | Máquina de estados del proyecto, errores 400/401/403/404/409/503, reversión de la asignación, avisos que no bloquean, reintentos de los consumidores, reconexión tras caerse RabbitMQ |
| Pruebas en Postman y evidencias de ejecución | 15 % | 67 peticiones y 146 verificaciones, casos de error incluidos, reproducibles con `newman`; `docs/GUIA-DE-PRUEBAS.md` |
| Documentación técnica y sustentación | 10 % | Este documento, el diseño, la referencia de la API, README por servicio, la guía de pruebas y el informe |

## 2. Mensaje de la docente en el Classroom

| # | Requisito | Estado |
|---|---|---|
| 32 | Comunicación síncrona entre User Service, Validation Service y Question Service | Cumple: question→user (autor con rol AUTOR) y question→validation |
| 33 | Comunicación asíncrona con Lifecycle, Review y Notification, usando colas y respuestas 202 | Cumple |
| 34 | Levantar la mensajería con Docker Compose y validar los flujos desde Postman | Cumple: `docker-compose.yml`, `scripts/levantar.*` y la colección |
| 35 | Trabajo en parejas (máximo 2) | Cumple: Edward y Laura |
| 36 | Monorepo con una carpeta por microservicio (`user-service`, `question-service`, `validation-service`, `lifecycle-service`, `review-service`, `notification-service`) | Cumple, con tres carpetas más |
| 37 | Carpeta `/postman` con el `.json` configurado con variables de entorno | Cumple: colección y entorno `SaberPro_Local` |
| 38 | Informe en la plantilla `Formato-Documento.docx`, con el enlace al repositorio público y capturas de RabbitMQ (`localhost:15672`) | **Pendiente**: qué capturar está en `docs/GUIA-DE-PRUEBAS.md` |
| 39 | Nomenclatura `lisw2_taller09_gX.docx` | **Pendiente**: `lisw2_taller09_g8.docx` |
| 40 | La guía es solo orientadora | Se siguió y, donde choca con el enunciado, manda el enunciado |

## 3. Guía paso a paso

| # | Requisito | Dónde y cómo se cumple | Estado |
|---|---|---|---|
| 41 | Tabla de microservicios y puertos 8081 a 8086, con su mecanismo de comunicación | Mismos puertos y mecanismos (los tres servicios nuevos usan 8087 a 8089) | Cumple |
| 42 | Monorepo `lisw2_taller09_microservicios_gX/` con `docker-compose.yml` y `postman/SaberPro_Microservices.postman_collection.json` | Carpeta `lisw2_taller09_microservicios_g8/`; mismo nombre de la colección | Cumple |
| 43 | Cada servicio autónomo con `config`, `controller`, `dto`, `service` y `messaging` | Mismo esquema, más `model` y `repository` (ver la correspondencia de archivos en 4) | Cumple |
| 44 | Paso 1: `docker-compose.yml` con `rabbitmq:3-management`, puertos 5672 y 15672, usuario y clave `guest` | `docker-compose.yml` (sin la clave `version`, obsoleta, y con comprobación de salud) | Cumple |
| 45 | Paso 2: Question llama síncronamente a Validation antes de guardar en DRAFT | `ValidationClient` | Cumple |
| 46 | Paso 3: comando por RabbitMQ y respuesta 202 con `operationId` | La respuesta usa las claves `message`, `operationId` y `status: PROCESSING` | Cumple |
| 47 | Paso 4: Lifecycle valida y pasa a PENDING_REVIEW; Review pide IN_REVIEW y publica `ReviewerAssigned` | `LifecycleService`, `ReviewService` | Cumple |
| 48 | Código 1: `RabbitMQConfig` con exchange `saberpro.direct.exchange`, cola durable, binding y `Jackson2JsonMessageConverter` | `config/RabbitMQConfig.java` de cada servicio; la cola se llama `lifecycle.commands` (Figura 2) | Cumple con diferencia (5.1) |
| 49 | Código 2: controlador que responde 202 y publica con `RabbitTemplate` | `QuestionController` + `CommandPublisher` | Cumple |
| 50 | Código 3: `@RabbitListener` en Lifecycle que procesa la transición | `CommandListener` | Cumple |
| 51 | Una base de datos por microservicio | H2 en memoria propia por servicio, sin tablas compartidas | Cumple |
| 52 | Serialización JSON en RabbitMQ | `Jackson2JsonMessageConverter` en cada servicio | Cumple |
| 53 | `@ExceptionHandler` para devolver 400 ante fallos de validación | `ApiExceptionHandler` (`@RestControllerAdvice`) en cada servicio | Cumple |
| 54 | `.gitignore` con `target/`, `.mvn/`, `.idea/`, `.DS_Store` | `.gitignore` de la raíz | Cumple |
| 55 | Commits incrementales identificando el microservicio | Plan en `docs/PLAN-DE-COMMITS.md`; los commits se hacen al subirlo | Pendiente (al subir) |
| 56 | Colección por carpetas (una por microservicio) con variables `{{user_service_url}}` (8081) y `{{question_service_url}}` (8083) | `postman/` | Cumple |
| 57 | Pruebas de casos exitosos y fallidos: credenciales incorrectas, fallos de validación estructural y transiciones no permitidas | Colección de Postman | Cumple |

### Correspondencia de archivos de la guía

| Archivo de la guía | Archivo del proyecto |
|---|---|
| `user-service/.../controller/AuthController.java`, `service/UserService.java` | Igual |
| `validation-service/.../controller/ValidationController.java`, `service/ValidationService.java` | `ValidationController`, `service/QuestionValidator.java` |
| `question-service/.../config/RabbitMQConfig.java`, `client/ValidationClient.java`, `controller/QuestionController.java`, `messaging/CommandPublisher.java` | Igual |
| `question-service/.../dto/QuestionDTO.java`, `SubmitQuestionCommand.java` | `dto/Dtos.java` (`PreguntaRequest`, `PreguntaDto`) y el comando como `EventEnvelope` |
| `lifecycle-service/.../controller/LifecycleController.java`, `messaging/CommandListener.java`, `service/LifecycleService.java` | Igual |
| `lifecycle-service/.../model/QuestionStatus.java` | `model/EstadoPregunta.java` (los siete estados del proyecto, con equivalente en inglés) |
| `review-service/.../controller/ReviewController.java`, `service/ReviewService.java` | Igual |
| `review-service/.../messaging/EventPublisher.java` | `messaging/ReviewEventPublisher.java` |
| `notification-service/.../messaging/NotificationListener.java` | Igual |
| `notification-service/.../service/EmailService.java` | `service/EmailSimulator.java` |

## 4. Resultados de las pruebas

| Prueba | Resultado |
|---|---|
| Pruebas unitarias y de integración (`mvn clean package`) | 78 pruebas, 0 fallos: user 5, validation 7, question 14, lifecycle 17, review 14, notification 7, history 4, simulation 6, tracking 4 |
| Colección de Postman con `newman` contra los nueve servicios y RabbitMQ | 67 peticiones, 146 verificaciones, 0 fallos, en dos ejecuciones seguidas sin reiniciar |
| RabbitMQ después de la colección | Nueve colas con un consumidor cada una y cero mensajes pendientes |
| RabbitMQ caído | El login sigue respondiendo 200; el envío a revisión responde 503 con el motivo; al volver, los consumidores se reconectan solos |
| Arranque con `scripts/levantar.sh` | Los nueve servicios responden y no hay errores en los registros |

## 5. Diferencias entre los documentos y decisiones

1. **Cola de comandos.** La guía la llama `question.submit.review.queue`; la Figura 2 del enunciado, `lifecycle.commands`.
   Se usa la del enunciado, con el exchange `saberpro.direct.exchange` de la guía y la routing key `question.submit`
   que coincide en ambos. Crear las dos colas haría que el comando se procesara dos veces.
2. **Ruta de transición.** La guía usa `POST /lifecycle/transition`; la Figura 2, `POST /lifecycle/{questionId}/transition`.
   Existen las dos.
3. **Estados.** La guía lista cuatro y el enunciado y la Figura 1 siete. Se usan los siete del proyecto, con su nombre
   en español y su equivalente en inglés en cada respuesta (`estado` y `equivalente`).
4. **Token.** El paso 1 dice que no hay tokens y el paso 2 pide el token en `Authorization`. Se acordó con la docente:
   el login devuelve un token simple y `question-service` exige el encabezado sin validarlo (el JWT es del corte 3).
5. **Creación y envío.** La Figura 2 junta la creación y el envío en un solo recorrido; el texto del escenario y la
   matriz los separan (crear devuelve 201, enviar devuelve 202). Se sigue el texto.
6. **Mensajes.** `ReviewerAssigned` se publica una vez por revisor (como en la Figura 1) con la routing key
   `reviewer.assigned` y la cola `notification.reviewer-assigned` de la Figura 2. Los demás avisos llegan a
   `notification.events`. El mensaje de pregunta enviada lleva `operationId` y `newState`.
7. **Notification «solo listener».** La guía lo describe como asíncrono puro. Además de los listeners tiene
   `GET /notifications` para poder comprobar en Postman que el aviso se generó.
8. **Cliente REST.** Se usa `RestClient` (Spring Web) en lugar de `RestTemplate` o `WebClient`, que la guía nombra
   como opciones.
9. **Un revisor o varios.** El enunciado habla de «un revisor»; el proyecto exige de 1 a 3, y el servicio acepta ese rango.
10. **Orden de la asignación.** La Figura 2 pide primero la transición y luego registra la asignación. Aquí se guarda
    la asignación y se pide la transición dentro de la misma transacción; si falla, se revierte. El resultado es el mismo.
11. **Servicios extra.** `history`, `simulation` y `tracking`, además de los seis, para mostrar contextos propios.
