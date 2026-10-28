# Taller 10: persistencia con JPA

Taller del Laboratorio de Ingeniería de Software II (octubre de 2026), en pareja: Edward Esteban Dávila Salazar
y Laura Isabel Sánchez Fernández (grupo 2). Persiste un modelo de dominio del banco de preguntas con JPA y H2 sin
poner anotaciones de persistencia en las clases de negocio. Entrega el 31 de octubre de 2026 a las 23:59.

El código, los nombres, los comentarios y el README del proyecto están en inglés porque lo pide el enunciado.

| Carpeta o archivo | Contenido |
|---|---|
| [`lisw2_taller10_jpa_g2/`](lisw2_taller10_jpa_g2/) | Proyecto Spring Boot con JPA y H2, con su README, los diagramas UML y entidad-relación y las pruebas |
| [`lisw2_taller10_g2.docx`](lisw2_taller10_g2.docx) | Informe del taller (el texto está en [`INFORME.md`](INFORME.md)) |
| [`docs/PLAN.md`](docs/PLAN.md) | Qué pide el enunciado, decisiones de diseño, tareas y calendario |
| [`docs/PLAN-DE-COMMITS.md`](docs/PLAN-DE-COMMITS.md) | Los commits previstos, con autor y fecha |

## Estado

Código terminado: 159 pruebas pasan (dominio, servicios con repositorios en memoria, persistencia sobre H2, API de punta a
punta y reglas de arquitectura), la aplicación arranca y el flujo completo funciona por HTTP, y el JavaDoc se genera sin
avisos. El informe está en `lisw2_taller10_g2.docx` (fuente en `INFORME.md`, imágenes en `img/`): parte I con el análisis
del demo, los diagramas UML y relacional, las pruebas y las respuestas de sustentación.

Para correrlo: `mvn test` o `mvn spring-boot:run` dentro de `lisw2_taller10_jpa_g2`, con Java 17 o superior.
