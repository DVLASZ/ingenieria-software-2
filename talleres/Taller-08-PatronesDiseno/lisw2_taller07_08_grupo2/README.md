# lisw2_taller07_08_grupo2

Proyecto Maven del Taller 8 (patrones de diseño), grupo 2. El contexto y las decisiones de diseño están en la carpeta
superior: [`README.md`](../README.md).

Requiere Java 17 o superior y Maven. No tiene dependencias en ejecución; JUnit 5 solo se usa en las pruebas.

## Paquetes (`src/main/java/co/edu/unicauca/saberpro`)

| Paquete | Patrón | Contenido |
|---|---|---|
| `builder` | Builder | `IQuestionBuilder`, `QuestionBuilder`, `QuestionDirector`, `InvalidQuestionException` |
| `adapter` | Adapter | `ExternalQuestionService` (servicio simulado), `QuestionProvider`, `ExternalQuestionAdapter` |
| `facade` | Facade | `QuestionBankFacade`, `ImportSummary`, `QuestionBankException` |
| `state` | State | Parte 2, pendiente |
| `templatemethod` | Template Method | Parte 2, pendiente |
| `decorator` | Decorator | Parte 2, pendiente |
| `domain` | (soporte) | `model` (`Question`, `Option`, `Context`, ...), `repository`, `service` y `validation`: los subsistemas que oculta la fachada |

`ApplicationMain` es la demostración: por ahora recorre la Parte 1 a través de la fachada y muestra el resultado por
consola.

## Pruebas (`src/test/java/co/edu/unicauca/saberpro`)

Una clase de prueba por patrón, más la de los subsistemas del banco: `builder/QuestionBuilderTest`,
`adapter/ExternalQuestionAdapterTest`, `domain/QuestionServiceTest` y `facade/QuestionBankFacadeTest`. En total son 51
pruebas.

## Comandos

```bash
mvn test       # ejecuta las 51 pruebas
mvn compile    # compila
java -Dfile.encoding=UTF-8 -cp target/classes co.edu.unicauca.saberpro.ApplicationMain
```

En IntelliJ IDEA: abrir esta carpeta como proyecto Maven y ejecutar `ApplicationMain` o las pruebas desde el IDE.
