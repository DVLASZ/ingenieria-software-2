# Dominio del banco de preguntas Saber Pro

Taller 11 del Laboratorio de Ingeniería de Software II (grupo 2). Es el modelo de dominio del banco de preguntas hecho con DDD y escrito
en Java puro: no hay Spring, ni JPA, ni ninguna librería de producción. Lo único que hay fuera del JDK es JUnit 5 para las pruebas.

## Qué tiene

- `Pregunta`: raíz del agregado. Guarda el enunciado, la justificación y las opciones, y no deja enviar a revisión una pregunta que no
  tenga exactamente 4 opciones y 1 clave.
- `Enunciado`, `Justificacion` y `OpcionRespuesta`: objetos de valor inmutables (son `record`). Cada uno se valida al crearse.
- `EstadoPregunta`: los siete estados y las transiciones que se permiten.
- `Revision`: raíz de otro agregado, el de la revisión por pares. Asigna de 1 a 3 revisores y decide por unanimidad. Usa `Revisor`
  (entidad), `Asignacion`, `Evaluacion` y `Dictamen`.
- `FiltroExpresion` y `ValidezOpciones`: las reglas del contexto de validación.
- `PreguntaRepository` y `RevisionRepository`: contratos, sin ninguna tecnología detrás.
- `DomainException` y `ReglaNegocioException`: lo que se lanza cuando el negocio no lo permite.

```
src/main/java/co/edu/unicauca/saberpro/domain/
├── model/        Pregunta, Revision, Revisor, objetos de valor, enums y reglas
├── exception/    DomainException, ReglaNegocioException
└── repository/   PreguntaRepository, RevisionRepository
```

## Cómo correrlo

Necesita Java 17 o superior y Maven.

```bash
mvn test
```

Son 77 pruebas. Una de ellas (`DominioPuroTest`) lee el código fuente y falla si alguna clase del dominio importa algo que no sea del
JDK o del propio dominio.

## Ejemplo

```java
Pregunta pregunta = new Pregunta("docente-1");
pregunta.redactarEnunciado("¿Cuál es la idea principal del texto?");
pregunta.justificar("La idea principal resume el argumento central del autor.");
pregunta.agregarOpcion("El autor defiende una tesis", true);
pregunta.agregarOpcion("El autor cuenta una historia", false);
pregunta.agregarOpcion("El autor lista datos", false);
pregunta.agregarOpcion("El autor da instrucciones", false);
pregunta.enviarARevision();   // con menos de 4 opciones lanza ReglaNegocioException
```
