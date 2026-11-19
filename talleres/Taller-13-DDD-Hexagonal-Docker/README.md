# Taller 13: entrega final con DDD, hexagonal y Docker

Entrega final del Laboratorio de Ingeniería de Software II (noviembre de 2026), en pareja: Edward Esteban Dávila Salazar y Laura Isabel
Sánchez Fernández (grupo 2). Junta el dominio puro del Taller 11, la arquitectura hexagonal del Taller 12 y los tres servicios del Parcial 2, y
los levanta con Docker Compose en la red saberpro-net. Entrega el 18 de noviembre de 2026 a las 23:59.

| Carpeta o archivo | Contenido |
|---|---|
| [`saberpro-banco-preguntas/`](saberpro-banco-preguntas/) | El repositorio de la entrega: tres servicios Spring Boot, sus Dockerfile, el `docker-compose.yml` y la colección de Postman |
| [`INFORME.md`](INFORME.md) | El informe: trazabilidad, dominio, hexágono, Docker, flujo de punta a punta, intercambiabilidad, C4 y pruebas |
| [`lisw2_taller13_g2.docx`](lisw2_taller13_g2.docx) | El mismo informe en la plantilla oficial |
| [`img/`](img/) | Los diagramas C4 y las capturas del informe |
| Video | [Sustentación en YouTube](https://www.youtube.com/watch?v=fAi0qf4DYCg) |

Para correrlo: `docker compose up -d --build` dentro de `saberpro-banco-preguntas`, con Docker instalado.
