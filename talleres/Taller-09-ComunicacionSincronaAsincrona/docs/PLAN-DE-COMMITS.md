# Taller 09: plan de commits

La guía pide publicar los commits de manera incremental, identificando claramente qué microservicio se está
desarrollando. Cada commit compila por sí solo: el `pom.xml` de la raíz va registrando los módulos a medida que se
agregan.

| # | Jira | Mensaje | Contenido |
|---|---|---|---|
| 1 | TALL9-2 | `Taller 9: estructura del proyecto, RabbitMQ con Docker Compose y scripts` | `pom.xml` sin módulos, `docker-compose.yml`, `.gitignore`, `scripts/`, README de la raíz |
| 2 | TALL9-3 | `user-service: login con roles y eventos de inicio de sesión` | Módulo `user-service` completo, con sus pruebas |
| 3 | TALL9-4 | `validation-service: validación estructural de la pregunta` | Módulo `validation-service` |
| 4 | TALL9-5 | `lifecycle-service: único dueño del estado, comandos y transiciones` | Módulo `lifecycle-service` |
| 5 | TALL9-6 | `question-service: crear preguntas, 202 Accepted y consulta de operaciones` | Módulo `question-service` |
| 6 | TALL9-7 | `review-service: asignación, evaluación por unanimidad y rondas` | Módulo `review-service` |
| 7 | TALL9-8 | `notification-service: avisos asíncronos con correo simulado` | Módulo `notification-service` |
| 8 | TALL9-9 | `history-service: historial por pregunta y auditoría` | Módulo `history-service` |
| 9 | TALL9-10 | `simulation-service: catálogo de preguntas publicadas y simulacros` | Módulo `simulation-service` |
| 10 | TALL9-11 | `tracking-service: estadísticas por usuario` | Módulo `tracking-service` |
| 11 | TALL9-12 | `Postman: colección y entorno con los casos de éxito y de error` | Carpeta `postman/` |
| 12 | TALL9-13 | `Documentación del Taller 9: diseño, trazabilidad, API y guía de pruebas` | Carpeta `docs/` y el README de `talleres/` |
| 13 | TALL9-14 | `Informe del Taller 9` | `INFORME.md`, imágenes y el documento Word |

Cada commit corresponde a una historia del espacio de Jira `TALL9` (tablero «TALL9 board», sprint «TALL9 Sprint 1», del 16 al 22 de
octubre de 2026, hasta el jueves a medianoche, fecha de entrega). Al subir un commit, su historia pasa a Finalizada con un comentario que enlaza el commit. La épica es TALL9-1.

Los commits se hacen sin línea de coautoría. Antes de cada uno se corre `mvn -pl <servicio> -am test`.
