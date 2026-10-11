# Taller 8: patrones de diseño creacionales, estructurales y de comportamiento

Laboratorio de Ingeniería de Software II, Universidad del Cauca (periodo 2026.2). Grupo 2: Edward Esteban Dávila
Salazar y Laura Isabel Sánchez Fernández.

Taller integrado sobre el banco de preguntas Saber Pro. Aplica seis patrones de diseño del catálogo GoF sobre un
modelo propio de pregunta, en un proyecto Maven independiente con la estructura de paquetes que pide la guía.

> Estado: está publicada la Parte 1 (Builder, Adapter y Facade). La Parte 2 (State, Template Method y Decorator) y el
> informe se publican en un segundo paso.

## Contenido

| Elemento | Qué es |
|---|---|
| [`lisw2_taller07_08_grupo2/`](lisw2_taller07_08_grupo2) | Proyecto Maven con el código y las pruebas (con su propio [`README`](lisw2_taller07_08_grupo2/README.md)) |

## Patrones

| Parte | Patrón | Tipo | Dónde | Estado |
|---|---|---|---|---|
| 1 (clase) | Builder | Creacional | `builder/` | Hecho |
| 1 (clase) | Adapter | Estructural | `adapter/` | Hecho |
| 1 (clase) | Facade | Estructural | `facade/` | Hecho |
| 2 (autónoma) | State | Comportamiento | `state/` | Pendiente |
| 2 (autónoma) | Template Method | Comportamiento | `templatemethod/` | Pendiente |
| 2 (autónoma) | Decorator | Estructural | `decorator/` | Pendiente |

## Estructura del proyecto

```
lisw2_taller07_08_grupo2/
├── pom.xml
└── src/
    ├── main/java/co/edu/unicauca/saberpro/
    │   ├── ApplicationMain.java       Demostración
    │   ├── builder/                   IQuestionBuilder, QuestionBuilder, QuestionDirector
    │   ├── adapter/                   ExternalQuestionService, QuestionProvider, ExternalQuestionAdapter
    │   ├── facade/                    QuestionBankFacade, ImportSummary, QuestionBankException
    │   ├── state/                     Estados del ciclo de vida (Parte 2, pendiente)
    │   ├── templatemethod/            Evaluadores de respuestas (Parte 2, pendiente)
    │   ├── decorator/                 Decoradores de preguntas (Parte 2, pendiente)
    │   └── domain/
    │       ├── model/                 Question, Option, Context, Classification, Competence, Difficulty
    │       ├── repository/            QuestionRepository y su versión en memoria
    │       ├── service/               QuestionService
    │       └── validation/            QuestionValidator
    └── test/java/co/edu/unicauca/saberpro/   Pruebas JUnit 5
```

Los paquetes `domain/repository`, `domain/service` y `domain/validation` son los subsistemas que oculta la fachada.

## Cómo compilar y ejecutar

Requiere Java 17 o superior y Maven. En IntelliJ IDEA se abre la carpeta `lisw2_taller07_08_grupo2` como proyecto
Maven (no la carpeta del taller).

```bash
cd lisw2_taller07_08_grupo2
mvn test       # ejecuta las 51 pruebas
mvn compile    # compila
java -Dfile.encoding=UTF-8 -cp target/classes co.edu.unicauca.saberpro.ApplicationMain   # demostración (o ejecutar ApplicationMain desde el IDE)
```

## Decisiones de diseño

- **Preguntas verdadero/falso y de caso de estudio:** el Builder exige 4 opciones y 1 correcta, así que el banco
  guarda preguntas de selección múltiple. Los evaluadores de verdadero/falso y de caso de estudio (Parte 2) reciben
  sus datos de evaluación aparte, sin pasar por el Builder.
- **Servicio externo:** se simula con cadenas crudas de 9 campos separados por `|`, sin dependencias externas.
- **Estados:** la guía describe cuatro estados (Borrador, Pendiente de revisión, En revisión y Aprobada) aunque su
  árbol de archivos omite `PendingReviewState`; se implementarán los cuatro (confirmado por la docente).
- **Numeración:** la docente considera el Parcial 1 como el «Taller 7», por eso el proyecto se llama
  `lisw2_taller07_08_grupo2`: continúa el trabajo del parcial. Este taller se mantiene como Taller 8.
- **Fachada:** cubre lo básico (crear, importar, listar y buscar preguntas) y expone un solo tipo de error,
  `QuestionBankException`.
