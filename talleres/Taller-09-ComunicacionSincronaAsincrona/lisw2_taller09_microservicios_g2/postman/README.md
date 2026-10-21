# Colección de Postman

| Archivo | Contenido |
|---|---|
| `SaberPro_Microservices.postman_collection.json` | 67 peticiones con 146 verificaciones, organizadas por microservicio |
| `SaberPro_Local.postman_environment.json` | Variables de entorno con la URL de cada servicio |

Variables del entorno: `{{user_service_url}}` (8081), `{{validation_service_url}}` (8082), `{{question_service_url}}`
(8083), `{{lifecycle_service_url}}` (8084), `{{review_service_url}}` (8085), `{{notification_service_url}}` (8086),
`{{history_service_url}}` (8087), `{{simulation_service_url}}` (8088) y `{{tracking_service_url}}` (8089).

## Cómo ejecutarla

1. Levantar RabbitMQ y los nueve servicios (`scripts/levantar.sh`).
2. Importar la colección y el entorno en Postman y elegir el entorno `SaberPro_Local`.
3. Ejecutar la colección completa con el Collection Runner, de arriba abajo: el orden sigue el escenario y cada
   petición guarda en variables (`token`, `q1`, `op1`…) lo que la siguiente necesita.

Por consola: `npx newman run SaberPro_Microservices.postman_collection.json -e SaberPro_Local.postman_environment.json`.

## Qué recorre

Cada carpeta tiene peticiones de un solo microservicio. El escenario vuelve a algunos servicios más adelante, por eso
`question-service`, `lifecycle-service` y `review-service` aparecen dos o tres veces.

| Carpeta | Casos |
|---|---|
| 01 · user-service | Login válido (autor y administrador), login incorrecto (401), login sin datos (400), revisores elegibles |
| 02 · validation-service | Pregunta correcta y pregunta inválida con sus errores |
| 03 · question-service · crear y enviar | Crear válida (201), con un usuario que no es autor (403), sin `Authorization` (401), inválida (400); enviar a revisión (202), resultado COMPLETED y una transición que falla (FAILED) |
| 04 · lifecycle-service | Estado y equivalente en inglés, aprobar una pregunta en Borrador (409) por las dos rutas, resultado de la operación |
| 05 · review-service · asignar y evaluar | Asignar con 0, 4, el autor, sin ser administrador o sin enviar la pregunta (errores); aprobar sin revisión (409); asignar 2 revisores; unanimidad; rechazo sin observación (400) y con ella |
| 06 · question-service · resultado, corrección y publicación | Aprobada y Rechazada, corregir y reenviar, publicar y archivar |
| 07 · lifecycle-service · motivo del rechazo | Motivo registrado, Publicada y Archivada |
| 08 · review-service · segunda ronda | Nueva ronda tras la corrección, asignación y aprobación |
| 09 · notification-service | Avisos al revisor, al autor y al administrador |
| 10 · history-service | Línea de tiempo de la pregunta y auditoría de inicios de sesión fallidos |
| 11 · simulation-service | Catálogo, crear y resolver un simulacro, y los errores |
| 12 · tracking-service | Estadísticas por usuario y resumen |

Las peticiones que dependen de un mensaje de RabbitMQ esperan 1,5 segundos antes de consultar (script previo).
