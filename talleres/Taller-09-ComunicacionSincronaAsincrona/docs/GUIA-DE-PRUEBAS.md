# Taller 09: guía de pruebas y evidencias

Cómo ejecutar el taller completo, qué resultado esperar y qué capturar para el informe. Todo corre en local.

## 1. Preparar el entorno

Requisitos: Java 21, Maven 3.9 o superior, Docker Desktop encendido (o un RabbitMQ instalado en el equipo) y, para
ejecutar Postman por consola, Node.js.

```bash
cd lisw2_taller09_microservicios_g2
mvn clean package            # compila los nueve servicios y corre sus 78 pruebas
./scripts/levantar.sh        # RabbitMQ con Docker + los nueve servicios (en PowerShell: .\scripts\levantar.ps1)
```

El script espera a que RabbitMQ esté saludable y a que cada servicio responda; si algo falla, indica cuál y dónde mirar
(`logs/<servicio>.log`). Sin Docker: `./scripts/levantar.sh --sin-docker` con un RabbitMQ ya instalado en `localhost:5672`.

Para detener todo: `./scripts/detener.sh --rabbit`.

## 2. Pruebas automáticas

`mvn clean package` corre 78 pruebas que no necesitan RabbitMQ (los mensajes se verifican con un `RabbitTemplate`
simulado y los listeners se invocan directamente):

| Servicio | Pruebas | Qué verifican |
|---|---|---|
| user | 5 | Login válido e incorrecto, eventos de inicio de sesión, revisores elegibles |
| validation | 7 | Cada regla de validación y la publicación de `ValidationFailed` |
| question | 14 | Crear (201, 400, 401, 403, 503), enviar (202), resultado asíncrono, edición, corrección y fallo de RabbitMQ |
| lifecycle | 17 | Los siete estados y sus transiciones, comandos COMPLETED y FAILED, idempotencia, las dos rutas de transición |
| review | 14 | De 1 a 3 revisores, elegibilidad, reversión si falla lifecycle, unanimidad, rechazo, rondas |
| notification | 7 | Cada tipo de aviso y la idempotencia |
| history | 4 | Línea de tiempo, idempotencia y auditoría |
| simulation | 6 | Catálogo, calificación y errores |
| tracking | 4 | Acumulación por usuario e idempotencia |

## 3. Colección de Postman

Con los servicios levantados:

- En Postman: importar `postman/SaberPro_Microservices.postman_collection.json` y
  `postman/SaberPro_Local.postman_environment.json`, elegir el entorno `SaberPro_Local` y ejecutar la colección completa
  con el Collection Runner.
- Por consola: `npx newman run postman/SaberPro_Microservices.postman_collection.json -e postman/SaberPro_Local.postman_environment.json`.

Resultado esperado: **67 peticiones y 146 verificaciones, todas en verde**. Se puede repetir sin reiniciar nada.

### Escenario que recorre la colección

1. **01 user-service:** se autentican el autor y el administrador y se obtienen los revisores.
2. **02 validation-service:** una pregunta válida y una inválida.
3. **03 question-service:** se crean tres preguntas (y los casos de error); la 1 y la 2 se envían a revisión (202) y se
   consulta el resultado de la operación; un segundo envío de la 1 queda `FAILED`.
4. **04 lifecycle-service:** estados, la transición rechazada (aprobar en Borrador) por las dos rutas.
5. **05 review-service:** errores de asignación, asignación de dos revisores a la 1 y aprobación por unanimidad; un
   revisor para la 2, que la rechaza con una observación.
6. **06 question-service:** la 1 quedó Aprobada y la 2 Rechazada; se corrige la 2 y se reenvía; se publica la 1 y se archiva la 3.
7. **07 lifecycle-service:** el motivo del rechazo, y la 1 Publicada y la 3 Archivada.
8. **08 review-service:** la segunda ronda de la pregunta 2 y su aprobación.
9. **09 notification-service, 10 history-service:** avisos e historial generados por los eventos.
10. **11 simulation-service, 12 tracking-service:** la pregunta publicada llega al catálogo, se resuelve un simulacro y
    el resultado aparece en las estadísticas.

## 4. Demostrar lo asíncrono

En Postman, tras «Enviar la pregunta 1 a revisión (202)», la respuesta trae el `operationId` pero no dice que la transición
terminó. «Resultado de la operación 1» (`GET /operations/{operationId}`) devuelve `COMPLETED` un instante después. El
segundo envío de la misma pregunta devuelve 202 y su operación queda `FAILED` con el motivo, sin informar falsamente de
éxito.

## 5. Resiliencia (para mostrar en la sustentación)

1. Con todo levantado, detener RabbitMQ: `docker stop saberpro-rabbitmq`.
2. `POST /auth/login` sigue respondiendo 200 (el evento de inicio de sesión solo se registra en el log).
3. `POST /questions/{id}/submit` responde 503 con «RabbitMQ no está disponible», y la operación queda `FAILED`.
4. Reiniciar: `docker start saberpro-rabbitmq`. A los pocos segundos los nueve consumidores se reconectan solos.

## 6. Capturas del panel de RabbitMQ para el informe

Abrir http://localhost:15672 (usuario y clave `guest`) **después de ejecutar la colección**:

| Captura | Dónde | Qué debe verse |
|---|---|---|
| Resumen | Pestaña *Overview* | Gráfica de mensajes publicados y entregados, y las conexiones de los servicios |
| Exchanges | Pestaña *Exchanges* | `saberpro.direct.exchange` (direct) y `saberpro.topic.exchange` (topic) |
| Colas | Pestaña *Queues and Streams* | Las nueve colas con 1 consumidor cada una y 0 mensajes pendientes (`Ready`) |
| Bindings de un exchange | *Exchanges* → `saberpro.topic.exchange` | Las colas y las routing keys enlazadas |
| Una cola en detalle | *Queues* → `lifecycle.commands` | Tasa de mensajes y el consumidor conectado |

Para ver los mensajes en tránsito, detener antes un servicio consumidor (por ejemplo `notification-service`), ejecutar el
escenario y mirar cómo se acumulan los mensajes en `notification.events` (se comprobó: quedan 8 mensajes listos) y en
`notification.reviewer-assigned` (4); al volver a levantarlo se consumen y las colas quedan en cero. Con ese servicio
detenido, solo fallan las peticiones de la carpeta 09, que lo consultan.

## 7. Usuarios y datos de prueba

Todos con la clave `clave123`: `admin`, `autor1`, `autor2` y `revisor1` a `revisor4`. Las bases son H2 en memoria: al
reiniciar un servicio sus datos se pierden.
