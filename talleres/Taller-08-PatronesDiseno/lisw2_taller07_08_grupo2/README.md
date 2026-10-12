# lisw2_taller07_08_grupo2

Proyecto Maven del Taller 8 (patrones de diseño), grupo 2. El contexto, las decisiones de diseño y el informe están en
la carpeta superior: [`README.md`](../README.md) e [`INFORME.md`](../INFORME.md).

Requiere Java 17 o superior y Maven. No tiene dependencias en ejecución; JUnit 5 solo se usa en las pruebas.

## Paquetes (`src/main/java/co/edu/unicauca/saberpro`)

| Paquete | Patrón | Contenido |
|---|---|---|
| `builder` | Builder | `IQuestionBuilder`, `QuestionBuilder`, `QuestionDirector`, `InvalidQuestionException` |
| `adapter` | Adapter | `ExternalQuestionService` (servicio simulado), `QuestionProvider`, `ExternalQuestionAdapter` |
| `facade` | Facade | `QuestionBankFacade`, `ImportSummary`, `QuestionBankException` |
| `state` | State | `IQuestionState`, `QuestionLifecycle`, `DraftState`, `PendingReviewState`, `ReviewState`, `ApprovedState` |
| `templatemethod` | Template Method | `QuestionEvaluator`, `MultipleChoiceEvaluator`, `TrueFalseEvaluator`, `CaseStudyEvaluator`, `EvaluationResult` |
| `decorator` | Decorator | `QuestionComponent`, `BasicQuestion`, `QuestionDecorator`, `HighDifficultyQuestion`, `FeedbackQuestion` |
| `domain` | (soporte) | `model` (`Question`, `Option`, `Context`, ...), `repository`, `service` y `validation`: los subsistemas que oculta la fachada |

`ApplicationMain` es la demostración: recorre los seis patrones y muestra el resultado por consola.

## Pruebas (`src/test/java/co/edu/unicauca/saberpro`)

Una clase de prueba por patrón, más la de los subsistemas del banco: `builder/QuestionBuilderTest`,
`adapter/ExternalQuestionAdapterTest`, `domain/QuestionServiceTest`, `facade/QuestionBankFacadeTest`,
`state/QuestionStateTest`, `templatemethod/QuestionEvaluatorTest` y `decorator/QuestionDecoratorTest`. En total son 98
pruebas.

## Comandos

```bash
mvn test       # ejecuta las 98 pruebas
mvn compile    # compila
java -Dfile.encoding=UTF-8 -cp target/classes co.edu.unicauca.saberpro.ApplicationMain
```

En IntelliJ IDEA: abrir esta carpeta como proyecto Maven y ejecutar `ApplicationMain` o las pruebas desde el IDE.
