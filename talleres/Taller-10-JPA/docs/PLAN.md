# Taller 10: plan de trabajo

Taller de JPA del laboratorio de Ingeniería de Software II (grupo 2, en parejas). Entrega el **31 de octubre de 2026 a las 23:59**
(Classroom: informe `lisw2_taller10_g2.docx` en la plantilla oficial `Formato-Documento.docx` y enlace al repositorio público).
Hoy es 24 de octubre, así que quedan siete días.

## Qué hay que entregar

| Entregable | Dónde queda |
|---|---|
| Informe de la parte I (tabla de arquitectura, 5 preguntas, relaciones, herencia, esquema relacional, evidencia en H2) | `lisw2_taller10_g2.docx` |
| Diagrama de clases UML del banco de preguntas | Mermaid en el README y PNG en el informe |
| Diagrama entidad-relación con claves primarias y foráneas | Mermaid en el README y PNG en el informe |
| Código Java con JavaDoc, contratos de repositorio e implementaciones JPA | `lisw2_taller10_jpa_g2/` |
| Configuración de Spring Boot y H2 | `application.properties` |
| Pruebas automatizadas y de integración | `src/test` |
| README (propósito, arquitectura, cómo correr, ejemplos) | `lisw2_taller10_jpa_g2/README.md` |

## Reglas que fija el enunciado

- Código, nombres, comentarios y README en inglés. JavaDoc en todo lo público.
- Java 17 o más, Spring Boot, Spring Data JPA, Maven, H2 en memoria, JUnit 5.
- Arquitectura en capas: `api`, `application`, `domain` (con `model` y `repository`) e `infrastructure/jpa`
  (con `record`, `repository` y `mapper`). Sin carpetas `port`, `adapter`, `in` ni `out`, y sin
  usar esas palabras en el informe ni en el README (el enunciado pide capas simples).
- El dominio no importa `jakarta.persistence` ni lleva anotaciones JPA. Los servicios no dependen de `JpaRepository`.
- Herencia con `InheritanceType.JOINED` (lo pide el Classroom). Relaciones: `@OneToOne`, `@OneToMany`, `@ManyToOne`, `@ManyToMany`.
- Los cambios de estado pasan por reglas de negocio y transacciones, no por actualizar una columna.

## Decisiones de diseño

- **Alcance:** `User`, `Competency`, `Topic`, `Question` (con `MultipleChoiceQuestion` y `TrueFalseQuestion`), `AnswerOption`,
  `QuestionMetadata` y `Quiz`. No se modelan revisiones, intentos ni resultados: el enunciado dice que no hace falta todo.
- **Uno a uno:** `QuestionMetadata` es el lado propietario, con la clave foránea `question_id` única y no nula. Así una pregunta no
  puede tener dos registros de metadatos y no hay metadatos sin pregunta.
- **Uno a muchos:** `Question` (selección múltiple) tiene muchas `AnswerOption`, con `question_id` no nula, cascada y
  `orphanRemoval`. Regla del dominio: **una opción correcta y cuatro distractores**, tal como dice la guía. Aviso en el README:
  el proyecto de curso usa cuatro opciones en total, y la cantidad es una constante del dominio.
- **Muchos a uno:** `Question` apunta a `User` (autor, obligatorio) y a `Topic` (opcional hasta clasificar).
- **Muchos a muchos:** `Question` y `Competency` con la tabla `question_competencies`. Se queda en `@ManyToMany` porque no hay atributos
  de la asociación. El informe explica cuándo habría que pasar a una entidad asociativa `QuestionCompetency` (por ejemplo con un peso).
  `Quiz` y `Question` también son muchos a muchos (`quiz_questions`).
- **Herencia:** `JOINED`. `questions` guarda lo común, `multiple_choice_questions` y `true_false_questions` lo propio. Se justifica en el
  informe frente a `SINGLE_TABLE` (muchas columnas nulas) y `TABLE_PER_CLASS` (consultas polimórficas con `UNION`).
- **Ciclo de vida:** `DRAFT, PENDING_REVIEW, IN_REVIEW, APPROVED, REJECTED, PUBLISHED, ARCHIVED`. Rechazada vuelve a borrador y cualquier
  estado puede archivarse (igual que en el proyecto). Un cuestionario solo acepta preguntas `PUBLISHED`.
- **Dos modelos:** el dominio es Java puro y las clases `*Record` llevan las anotaciones. Los mapeadores traducen. En el repositorio JPA, `save` carga
  el registro existente y le copia el estado, para no perder asociaciones al actualizar.
- **Paquete base:** `com.example.questionbank`, como en la estructura del enunciado. Boot 3.5.16, porque es la versión que ya está en la caché de Maven.

## Review focus (lo que el enunciado no dice y puede fallar)

1. Una pregunta de selección múltiple con dos respuestas correctas o con menos de cuatro distractores: el dominio la rechaza al enviarla a revisión.
2. Enviar a revisión sin tema o sin competencias: error de regla de negocio, el estado no cambia.
3. Autor, tema o competencia que no existen: `NotFoundException` y, a nivel de base de datos, la clave foránea lo impide.
4. Una pregunta con dos registros de metadatos: la restricción `UNIQUE` de `question_id` lo bloquea.
5. Leer asociaciones perezosas fuera de transacción: los mapeadores corren dentro de la transacción del repositorio y una prueba documenta el error si no.

## Estructura de archivos

```
talleres/Taller-10-JPA/
  README.md                         qué es y dónde está cada cosa
  docs/PLAN.md, PLAN-DE-COMMITS.md  este plan y los commits
  lisw2_taller10_jpa_g2/
    pom.xml, README.md, .gitignore
    src/main/java/com/example/questionbank/
      QuestionBankApplication.java
      api/            controllers, dto/, GlobalExceptionHandler
      application/    QuestionService, QuizService, UserService, CatalogService, command/
      domain/model/   User, Role, Competency, Topic, Question, MultipleChoiceQuestion, TrueFalseQuestion,
                      AnswerOption, QuestionMetadata, QuestionStatus, Difficulty, Quiz
      domain/exception/  BusinessRuleException, NotFoundException
      domain/repository/ QuestionRepository, UserRepository, CompetencyRepository, TopicRepository, QuizRepository
      infrastructure/jpa/record/      UserRecord, CompetencyRecord, TopicRecord, QuestionRecord (+2 subtipos),
                                      AnswerOptionRecord, QuestionMetadataRecord, QuizRecord
      infrastructure/jpa/repository/  interfaces Spring Data (Spring*DataRepository)
      infrastructure/jpa/mapper/      mapeadores dominio y registro
      infrastructure/jpa/*RepositoryJpa.java
    src/test/java/...  domain/, application/ (con repositorios en memoria), infrastructure/jpa/, api/, architecture/
```

## Tareas

Cada tarea termina con pruebas en verde (`mvn test` dentro de `lisw2_taller10_jpa_g2`).

1. **Esqueleto.** `pom.xml`, clase principal, `application.properties` (H2 con consola), `.gitignore`. Verifica: `mvn -q compile`.
2. **Datos de referencia del dominio.** `Role`, `User`, `Competency`, `Topic` y las dos excepciones. Pruebas: validaciones de campos obligatorios.
3. **Preguntas y ciclo de vida.** `Difficulty`, `QuestionStatus` (`canMoveTo`), `AnswerOption`, `QuestionMetadata`, `Question`,
   `MultipleChoiceQuestion`, `TrueFalseQuestion`. Primero las pruebas: transiciones válidas e inválidas, reglas de opciones, `submitForReview`.
4. **Cuestionarios.** `Quiz.addQuestion` rechaza preguntas no publicadas y duplicadas. Prueba de éxito y de error.
5. **Contratos de repositorio.** Cinco interfaces en `domain.repository`, sin tipos de Spring ni de JPA.
6. **Registros JPA.** Los `*Record` con las cuatro relaciones y `JOINED`, más las interfaces Spring Data. Prueba de que Hibernate valida y crea las tablas.
7. **Mapeadores y repositorios JPA.** Prueba con `@DataJpaTest`: guardar y leer los dos tipos, relaciones, consultas, actualización, claves foráneas y restricciones, carga perezosa.
8. **Servicios de aplicación.** Cinco casos de uso con transacciones. Pruebas con repositorios en memoria (sin JPA ni Spring).
9. **API REST.** Controladores, DTO con validación y manejador de errores. Pruebas con `MockMvc` de punta a punta, éxito y error.
10. **Arquitectura y JavaDoc.** Prueba ArchUnit de las reglas de dependencia, y `mvn javadoc:javadoc` sin errores.
11. **Documentación.** README del proyecto con los dos diagramas en Mermaid y ejemplos `curl`; README de la carpeta; fila en `talleres/README.md`.
12. **Informe.** Parte I (correr el demo, capturas de H2, tabla de arquitectura, respuestas, esquema relacional), UML y ER en PNG, enlace al repositorio,
    y el `.docx` en la plantilla oficial. Va después del código y no se empieza hasta que el código esté cerrado.

## Calendario

| Día | Trabajo |
|---|---|
| 24 y 25 de octubre | tareas 1 a 5 |
| 26 y 27 de octubre | tareas 6 a 8 |
| 28 de octubre | tareas 9 y 10 |
| 29 de octubre | tarea 11 y borrador de la parte I del informe |
| 30 de octubre | tarea 12, revisión de todo y repaso de las 6 preguntas de sustentación |
| 31 de octubre | margen. Entregar antes de las 23:59, sin dejarlo para la última hora |

## Sustentación

Las seis preguntas del enunciado se contestan con lo que se hizo: dos modelos (ventajas y costos), qué cambia si se quita JPA (solo
`infrastructure/jpa`), muchos a muchos frente a entidad asociativa, `LazyInitializationException` (hay una prueba que la provoca), por qué
`JOINED` con muchos tipos, y por qué guardar una pregunta y validar su ciclo de vida son responsabilidades distintas (`Question` y
`QuestionStatus` en el dominio, `QuestionRepositoryJpa` en infraestructura).
