# Taller 10: plan de commits

Un commit por cambio que se pueda probar solo, con los autores repartidos entre Edward y Laura. Los mensajes van en inglés (el enunciado
pide todo en inglés), en una línea, en minúscula y sin punto. Ninguno lleva línea de coautoría.

Antes de cada commit: `mvn test` en verde dentro de `lisw2_taller10_jpa_g2`. Después de los commits con código: `graphify update .` en la raíz del repositorio.

Hasta que se pida, no se hace ningún commit ni push. Las fechas de abajo son las previstas, no se adelantan.

| N | Fecha | Autor | Mensaje | Qué entra |
|---|---|---|---|---|
| 1 | 24 oct | Edward | `project skeleton with spring boot and h2` | `pom.xml`, clase principal, `application.properties`, `.gitignore` |
| 2 | 24 oct | Laura | `user, role, competency and topic in the domain` | modelo de referencia, excepciones y sus pruebas |
| 3 | 25 oct | Edward | `question lifecycle and status transitions` | `Difficulty`, `QuestionStatus`, `Question`, `AnswerOption`, `QuestionMetadata` y pruebas |
| 4 | 25 oct | Laura | `multiple choice and true false questions` | los dos subtipos, sus reglas y pruebas |
| 5 | 25 oct | Edward | `quiz accepts only published questions` | `Quiz` y pruebas de éxito y error |
| 6 | 26 oct | Laura | `repository contracts in the domain` | cinco interfaces en `domain.repository` |
| 7 | 26 oct | Edward | `jpa records with the four relations and joined inheritance` | `*Record` e interfaces Spring Data |
| 8 | 27 oct | Laura | `mappers and jpa repositories` | mapeadores y `*RepositoryJpa` |
| 9 | 27 oct | Edward | `persistence tests for relations, constraints and lazy loading` | pruebas `@DataJpaTest` |
| 10 | 28 oct | Laura | `application services with in-memory tests` | servicios, comandos y repositorios en memoria de las pruebas |
| 11 | 28 oct | Edward | `rest api with validation and error handler` | controladores, DTO, manejador de errores y pruebas `MockMvc` |
| 12 | 28 oct | Laura | `architecture rules and javadoc check` | prueba ArchUnit y ajustes de JavaDoc |
| 13 | 29 oct | Edward | `readme with uml and er diagrams` | README del proyecto con Mermaid y ejemplos |
| 14 | 29 oct | Laura | `taller 10 readme and plan` | README de la carpeta, `docs/` y fila en `talleres/README.md` |
| 15 | 30 oct | Laura | `report part one and diagrams` | texto de la parte I, PNG de UML y ER, capturas de H2 |
| 16 | 30 oct | Edward | `taller 10 report in word` | `lisw2_taller10_g2.docx` |

Si un día se atrasa, se mueve el siguiente, pero el informe (15 y 16) no baja del 30 de octubre para dejar el 31 de margen.
