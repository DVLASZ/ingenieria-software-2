# Taller 09: comunicación síncrona y asíncrona de microservicios

Taller del Laboratorio de Ingeniería de Software II (octubre de 2026), en pareja: Edward Esteban Dávila Salazar
y Laura Isabel Sánchez Fernández (grupo 8). Se aplica al sistema del Banco de Preguntas Saber PRO.

| Carpeta o archivo | Contenido |
|---|---|
| [`lisw2_taller09_microservicios_g8/`](lisw2_taller09_microservicios_g8/) | Proyecto: nueve microservicios Spring Boot, RabbitMQ y colección de Postman |
| [`INFORME.md`](INFORME.md) | Informe del taller (texto fuente) |
| [`lisw2_taller09_g8.docx`](lisw2_taller09_g8.docx) | Informe del taller en Word, con el formato del curso |
| [`img/`](img/) | Diagramas y capturas del informe (panel de RabbitMQ, newman, pruebas) |
| [`docs/TRAZABILIDAD-REQUISITOS.md`](docs/TRAZABILIDAD-REQUISITOS.md) | Cada requisito del enunciado, del Classroom y de la guía, y dónde se cumple |
| [`docs/API.md`](docs/API.md) | Endpoints de cada servicio y mensajes de RabbitMQ |
| [`docs/GUIA-DE-PRUEBAS.md`](docs/GUIA-DE-PRUEBAS.md) | Cómo ejecutar y comprobar todo, y qué capturar para el informe |
| [`docs/PLAN-DE-COMMITS.md`](docs/PLAN-DE-COMMITS.md) | Commits incrementales por microservicio |
| [`docs/DISENO-MICROSERVICIOS.md`](docs/DISENO-MICROSERVICIOS.md) | Diseño: nueve microservicios, estados y reglas del proyecto, mensajería, estructura y flujos |
| [`docs/ANALISIS-REQUISITOS.md`](docs/ANALISIS-REQUISITOS.md) | Comparación del enunciado y la guía, y decisiones de diseño |
| [`docs/PREGUNTAS-PROFESORA.md`](docs/PREGUNTAS-PROFESORA.md) | Dudas para resolver con la profesora |

## Estado

Desarrollo terminado e informe redactado: los nueve servicios, la colección de Postman y los scripts para
levantarlo todo. Las 78 pruebas unitarias y de integración pasan, y la colección de Postman (67 peticiones, 146
verificaciones) pasa completa contra los nueve servicios con RabbitMQ real. Se comprobó además que, si RabbitMQ se
cae, el login sigue respondiendo y el envío a revisión informa el error (503), que los consumidores se reconectan solos,
y que, con un consumidor detenido, los mensajes esperan en su cola y se procesan al volver.

## Decisiones acordadas con la profesora

- Los nueve servicios van completos, no solo los seis del taller.
- Los estados y la forma de rechazar son exactamente los del proyecto.
- El token es simple: el login lo devuelve y `question-service` exige el encabezado `Authorization` sin validarlo.
- El código vive en este repositorio, en esta carpeta; si se pide un repositorio nuevo, se separa.
