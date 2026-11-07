# Parcial 2 (segundo corte)

Segundo parcial práctico del laboratorio de Ingeniería de Software II, de la ingeniera Paola Andrea Bedoya Toro. Hay 80 minutos para resolver tres
puntos que juntan lo de los talleres 8, 9 y 10 sobre el banco de preguntas Saber Pro.

- `lisw2_parcial02_davilaE/`: proyecto Maven con tres microservicios y un módulo de dominio, tal como se entregó. Las pruebas de `src/test` se agregaron después.
- `lisw2_parcial02_corteII_B.pdf`: enunciado del parcial (grupo B).
- `lisw2_parcial02_davilaE.docx`: documento con las evidencias del proceso.
- `INFORME.md` y `img/`: el mismo documento en Markdown, con sus capturas.
- `postman/`: la colección de Postman con las ocho peticiones de la prueba.

## Qué pedía el enunciado

| Punto | Peso | Tiempo | Tema | Dónde está resuelto |
|---|---|---|---|---|
| 1 | 30 % | 24 min | Patrones Builder y State (taller 8) | `saberpro-domain`: `QuestionBuilder` y los estados `DraftState` y `PendingReviewState` |
| 2 | 40 % | 32 min | REST síncrono y AMQP con RabbitMQ (taller 9) | `question-service`, `validation-service` y `lifecycle-service` |
| 3 | 30 % | 24 min | Persistencia JPA limpia (taller 10) | `question-service`: `QuestionRecord`, `QuestionDomainMapper` y `QuestionRepositoryJpaImpl` |

## Los módulos

| Módulo | Puerto | Qué hace |
|---|---|---|
| `saberpro-domain` | | La clase `Question` sin anotaciones de persistencia, el Builder y el State. Lo usan los dos servicios con datos |
| `validation-service` | 8082 | `POST /validate`. La justificación tiene que tener más de 15 caracteres |
| `question-service` | 8083 | `POST /questions` y `POST /questions/{id}/submit`. Guarda con JPA en H2 |
| `lifecycle-service` | 8084 | Escucha `question.submit.review.queue` y pasa la pregunta de `DRAFT` a `PENDING_REVIEW` |

## Cómo funciona

### Punto 1: Builder y State

`QuestionBuilder.build()` arma la pregunta y, antes de crearla, revisa que haya enunciado, exactamente 4 opciones sin texto vacío y una sola opción
correcta. Si algo falla lanza `InvalidQuestionException` con todos los errores juntos.

El estado de la pregunta es un objeto: `IQuestionState` tiene la operación `submitForReview()`. `DraftState` devuelve `PendingReviewState` y
`PendingReviewState` lanza `InvalidTransitionException`. No hay ningún `if` ni `switch` para decidir la transición, cada estado sabe a cuál pasa.

### Punto 2: REST y RabbitMQ

`POST /questions` llama por REST, con `RestTemplate`, a `POST /validate` de `validation-service`. Si la justificación sirve, arma la pregunta con
el Builder, la guarda en `DRAFT` y responde 201. Si no, responde 400 y no guarda nada. Si `validation-service` no responde, el resultado es un 503.

`POST /questions/{id}/submit` publica el comando `SubmitQuestionForReview` en el exchange `saberpro.direct.exchange` con la routing key
`question.submit` y responde 202 Accepted con un `operationId`, sin esperar. `lifecycle-service` lo recibe con `@RabbitListener` en
`question.submit.review.queue` y hace la transición con el State.

```
POST /questions/{id}/submit
  question-service  --question.submit-->  question.submit.review.queue  -->  lifecycle-service
  question-service  <--question.status.changed--  question.status.changed.queue  <--  lifecycle-service
```

### Punto 3: persistencia

La clase de negocio `Question` está en `domain.model` y es Java puro. En `infrastructure.jpa.record` están `QuestionRecord`, anotada con `@Entity`,
y `AnswerOptionRecord`, unidas con `@OneToMany(cascade = CascadeType.ALL)`. `QuestionDomainMapper` convierte en los dos sentidos y
`QuestionRepositoryJpaImpl` usa `SpringQuestionDataRepository` para guardar en H2.

## Decisiones que no pedía el enunciado

- `lifecycle-service` avisa de vuelta con un mensaje `question.status.changed`, que `question-service` recibe en `question.status.changed.queue`. Sin
  ese aviso la transición ocurría en `lifecycle-service` pero la pregunta nunca cambiaba de estado en la base de datos.
- Enviar a revisión una pregunta que ya está en `PENDING_REVIEW` responde 409, y una que no existe responde 404.
- Un comando que nunca va a poder aplicarse, por ejemplo uno repetido, se rechaza sin volver a la cola, para que no quede dando vueltas.
- Cada servicio declara el exchange y las colas que usa. Declararlos dos veces no hace daño, y así el mensaje no se pierde sin importar qué servicio
  arranque primero.

## Cómo correrlo

Hace falta Java 17 o superior, Maven y un RabbitMQ en `localhost:5672` con el usuario y la clave `guest`. No se usa Docker ni base de datos externa:
la base es H2 en memoria y se vacía cada vez que `question-service` se reinicia.

1. Prender RabbitMQ y comprobar el panel en http://localhost:15672 (usuario `guest`, clave `guest`).
2. Compilar e instalar desde `lisw2_parcial02_davilaE`, para que los servicios encuentren el módulo `saberpro-domain`:

```bash
cd lisw2_parcial02_davilaE
mvn install -DskipTests
```

3. Arrancar los tres servicios, cada uno en su terminal:

```bash
mvn -pl validation-service spring-boot:run
mvn -pl lifecycle-service spring-boot:run
mvn -pl question-service spring-boot:run
```

Si `question-service` no puede conectarse a RabbitMQ, el envío a revisión responde 503. Si `validation-service` no está corriendo, crear una
pregunta responde 503.

## La prueba con Postman

Se importa `postman/saberpro-parcial2.postman_collection.json` y se corren las peticiones en orden. La tercera guarda sola el id de la pregunta que
crea, y la sexta y la séptima lo usan.

| Petición | Qué debe responder |
|---|---|
| 1. `validate`, justificación válida | 200 con `valid` en `true` |
| 2. `validate`, justificación corta | 200 con `valid` en `false` |
| 3. Crear pregunta válida | 201, estado `DRAFT` |
| 4. Crear con justificación corta | 400, no se guarda |
| 5. Crear con 3 opciones | 400, el Builder la rechaza |
| 6. Enviar a revisión | 202 con un `operationId` |
| 7. Enviar otra vez | 409, ya está en `PENDING_REVIEW` |
| 8. Enviar una que no existe | 404 |

El cuerpo de `POST http://localhost:8083/questions` tiene siempre 4 opciones y una sola correcta:

```json
{
  "statement": "¿Qué patrón separa el núcleo de los módulos?",
  "justification": "El microkernel deja el núcleo mínimo y carga plugins",
  "options": [
    { "text": "Microkernel", "correct": true },
    { "text": "Monolito", "correct": false },
    { "text": "Singleton", "correct": false },
    { "text": "Facade", "correct": false }
  ]
}
```

Después de la petición 6 se puede ver lo que pasó:

- En la consola de `lifecycle-service` aparece "Pregunta 1 pasó de DRAFT a PENDING_REVIEW", con el mismo `operationId` que devolvió Postman.
- En RabbitMQ, el exchange `saberpro.direct.exchange` es de tipo `direct` y tiene los bindings de `question.submit.review.queue` y
  `question.status.changed.queue`. La primera tiene un consumidor y no deja mensajes listos.
- En la consola de H2 (http://localhost:8083/h2-console, url `jdbc:h2:mem:questiondb`, usuario `sa`, clave vacía),
  `SELECT * FROM QUESTIONS` muestra el estado en `PENDING_REVIEW`. Las opciones están en `ANSWER_OPTIONS`.

Las capturas de la prueba están en [`INFORME.md`](INFORME.md).

## Pruebas automáticas (adicional, no se entregó)

Esto no hizo parte de la entrega ni de la nota. El proyecto que se entregó no tenía pruebas automáticas, porque la rúbrica pedía comprobarlo en
Postman y en la consola de RabbitMQ. Después del parcial se escribieron las pruebas y se agregaron a esta carpeta como complemento, sin cambiar nada del
código entregado: solo son los archivos nuevos de `src/test` en cada módulo.

| Módulo | Qué prueban |
|---|---|
| `saberpro-domain` | El Builder (4 opciones, una sola correcta, enunciado y opciones sin texto vacío) y las transiciones del State |
| `validation-service` | La regla de la justificación (más de 15 caracteres) y el endpoint `POST /validate` |
| `question-service` | Los casos de uso, el controlador, el mapeador entre `Question` y `QuestionRecord`, el repositorio JPA sobre H2, el cliente REST de validación, los mensajes y el flujo completo |
| `lifecycle-service` | La transición al recibir el comando, la declaración de colas de RabbitMQ y el arranque del servicio |

Son 72 pruebas y no necesitan RabbitMQ prendido. Se corren con:

```bash
cd lisw2_parcial02_davilaE
mvn test
```
